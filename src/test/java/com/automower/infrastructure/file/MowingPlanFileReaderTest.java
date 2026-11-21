package com.automower.infrastructure.file;

import com.automower.domain.MowingPlan;
import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class MowingPlanFileReaderTest {

    private static final String VALID_HEADER = "5 5\n1 2 N\n";

    @TempDir
    private Path tempDir;

    private final MowingPlanFileReader reader = new MowingPlanFileReader();

    @Test
    void shouldRejectWholeFile_whenPathDoesNotExist() {
        Path missing = tempDir.resolve("missing.txt");

        BusinessValidationException exception = reject(missing);

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.FILE_NOT_FOUND);
        assertThat(exception).hasMessage("file not found: " + missing);
        assertThat(exception.getLineNumber()).isEmpty();
    }

    @Test
    void shouldRejectWholeFile_whenPathIsDirectory() {
        BusinessValidationException exception = reject(tempDir);

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.FILE_NOT_READABLE);
        assertThat(exception).hasMessage("path is a directory: " + tempDir);
        assertThat(exception.getLineNumber()).isEmpty();
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void shouldRejectWholeFile_whenPermissionIsDenied() throws IOException {
        Path file = write(utf8(VALID_HEADER + "F\n"));
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("---------"));
        // root lit tous les fichiers quels que soient les droits : le cas n'est pas reproductible
        assumeFalse(Files.isReadable(file));

        BusinessValidationException exception = reject(file);

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.FILE_NOT_READABLE);
        assertThat(exception).hasMessage("file is not readable: " + file);
    }

    @Test
    void shouldRejectWholeFile_whenFileIsEmpty() throws IOException {
        BusinessValidationException exception = reject(write(new byte[0]));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.EMPTY_FILE);
        assertThat(exception).hasMessage("file is empty");
        assertThat(exception.getLineNumber()).isEmpty();
    }

    @Test
    void shouldReadFile_whenSizeIsExactlyLimit() throws IOException {
        Path file = write(validFileOfSize(MowingPlanFileReader.MAX_FILE_SIZE));

        MowingPlan plan = reader.read(file);

        assertThat(plan.programs()).hasSize(1);
        assertThat(plan.programs().getFirst().instructions()).hasSize(MowingPlanFileReader.MAX_FILE_SIZE - VALID_HEADER.length());
    }

    @Test
    void shouldRejectBeforeReadingContent_whenSizeExceedsLimitEvenWithFormatAndEncodingErrors() throws IOException {
        byte[] content = new byte[MowingPlanFileReader.MAX_FILE_SIZE + 1];
        // octets invalides en UTF-8 et en format : seule la taille doit être signalée
        Arrays.fill(content, (byte) 0xFF);

        BusinessValidationException exception = reject(write(content));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.FILE_TOO_LARGE);
        assertThat(exception).hasMessage("file size 10000001 bytes exceeds limit of 10000000 bytes");
        assertThat(exception.getLineNumber()).isEmpty();
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void shouldStopReadingAtLimit_whenSpecialFileAnnouncesSizeZeroButStreamsMore() throws Exception {
        Path fifo = tempDir.resolve("input.fifo");
        new ProcessBuilder("mkfifo", fifo.toString()).start().waitFor();
        // un pipe nommé annonce une taille de 0 : seule la lecture plafonnée borne la mémoire
        Thread producer = Thread.ofPlatform().daemon().start(() -> streamMoreThanLimit(fifo));

        BusinessValidationException exception = reject(fifo);

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.FILE_TOO_LARGE);
        assertThat(exception).hasMessage("file size exceeds limit of 10000000 bytes");
        assertThat(exception.getLineNumber()).isEmpty();
        producer.join(5_000);
    }

    @ParameterizedTest
    @MethodSource("invalidUtf8Contents")
    void shouldRejectWholeFile_whenContentIsNotValidUtf8(byte[] content) throws IOException {
        BusinessValidationException exception = reject(write(content));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_ENCODING);
        assertThat(exception).hasMessage("file is not valid UTF-8");
        assertThat(exception.getLineNumber()).isEmpty();
    }

    @Test
    void shouldReportFormatErrorWithLineNumber_whenNonAsciiCharacterIsValidUtf8() throws IOException {
        BusinessValidationException exception = reject(write(utf8("5 5\n1 é N\nF\n")));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_COORDINATE);
        assertThat(exception.getLineNumber()).hasValue(2);
    }

    private static Stream<Arguments> invalidUtf8Contents() {
        return Stream.of(
                Arguments.of((Object) concat(utf8(VALID_HEADER), new byte[]{(byte) 0xFF})),
                Arguments.of((Object) concat(utf8(VALID_HEADER), new byte[]{(byte) 0xC3})),
                Arguments.of((Object) concat(utf8(VALID_HEADER), new byte[]{(byte) 0xC0, (byte) 0xAF})),
                Arguments.of((Object) concat(utf8(VALID_HEADER), new byte[]{(byte) 0xED, (byte) 0xA0, (byte) 0x80})),
                // erreur de format en ligne 1 et encodage invalide plus loin : l'encodage prime
                Arguments.of((Object) concat(utf8("5 A\n1 2 N\n"), new byte[]{(byte) 0xFF}))
        );
    }

    private static void streamMoreThanLimit(Path fifo) {
        byte[] chunk = new byte[1 << 16];
        Arrays.fill(chunk, (byte) 'L');
        try (OutputStream output = Files.newOutputStream(fifo)) {
            for (long written = 0; written <= MowingPlanFileReader.MAX_FILE_SIZE; written += chunk.length) {
                output.write(chunk);
            }
        } catch (IOException exception) {
            // le lecteur ferme le pipe dès la limite dépassée : l'écriture restante échoue, c'est attendu
        }
    }

    private static byte[] validFileOfSize(int size) {
        byte[] content = new byte[size];
        byte[] header = utf8(VALID_HEADER);
        Arrays.fill(content, (byte) 'L');
        System.arraycopy(header, 0, content, 0, header.length);
        return content;
    }

    private BusinessValidationException reject(Path path) {
        return catchThrowableOfType(BusinessValidationException.class, () -> reader.read(path));
    }

    private Path write(byte[] content) throws IOException {
        Path file = Files.createTempFile(tempDir, "input", ".txt");
        Files.write(file, content);
        return file;
    }

    private static byte[] utf8(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
