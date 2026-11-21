package com.automower.infrastructure.cli;

import com.automower.application.MowLawnUseCase;
import com.automower.infrastructure.file.MowingPlanFileReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MowerCliIntegrationTest {

    @TempDir
    private Path tempDir;

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final MowerCli cli = newCli();

    /**
     * Entrée : exemple de référence complet (5 5 / 1 2 N / LFLFLFLFF / 3 3 E / FFRFFRFRRF)
     * Scénario : deux tondeuses exécutées l'une après l'autre, dans l'ordre du fichier
     * Résultat : stdout vaut exactement "1 3 N\n5 1 E\n", code de sortie 0
     */
    @Test
    void run_referenceExample_printsOneLinePerMowerInFileOrder() throws Exception {
        int exitCode = cli.run(new String[]{fixture("reference-example.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toByteArray()).isEqualTo("1 3 N\n5 1 E\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Entrée : pelouse 5 5, tondeuse 5 3 E, séquence FL
     * Scénario : le F ferait sortir la tondeuse par le bord est, il est ignoré sans erreur
     * Résultat : "5 3 N\n", code de sortie 0
     */
    @Test
    void run_forwardBlockedByEdge_ignoresInstructionAndContinues() throws Exception {
        int exitCode = cli.run(new String[]{fixture("blocked-by-edge.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("5 3 N\n");
    }

    /**
     * Entrée : pelouse rectangulaire 5 3, tondeuse 0 0 E, séquence FFFFFFL
     * Scénario : la tondeuse traverse la largeur, le 6e F est bloqué par le bord
     * Résultat : "5 0 N\n", code de sortie 0
     */
    @Test
    void run_rectangularLawn_usesIndependentBounds() throws Exception {
        int exitCode = cli.run(new String[]{fixture("rectangular-lawn.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("5 0 N\n");
    }

    /**
     * Entrée : pelouse 5 5, tondeuse 1 en 1 1 N avec L, tondeuse 2 en 0 1 E avec FL
     * Scénario : la tondeuse 1 s'arrête en 1 1, sa case bloque le F de la tondeuse 2 comme un bord
     * Résultat : "1 1 W\n0 1 N\n", code de sortie 0
     */
    @Test
    void run_forwardOntoStoppedMower_ignoresInstructionAndContinues() throws Exception {
        int exitCode = cli.run(new String[]{fixture("blocked-by-stopped-mower.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("1 1 W\n0 1 N\n");
    }

    /**
     * Entrée : pelouse 5 5, tondeuse 1 en 0 0 E avec FF, tondeuse 2 en 2 0 N avec L
     * Scénario : la tondeuse 2 n'a pas encore démarré, sa case de départ bloque le second F de la tondeuse 1
     * Résultat : "1 0 E\n2 0 W\n", code de sortie 0
     */
    @Test
    void run_forwardOntoMowerNotStartedYet_ignoresInstructionAndContinues() throws Exception {
        int exitCode = cli.run(new String[]{fixture("blocked-by-waiting-mower.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("1 0 E\n2 0 W\n");
    }

    /**
     * Entrée : pelouse aux bornes maximales 40075016 40075016, trois tondeuses dont une au coin supérieur droit
     * Scénario : la tondeuse 2 est bloquée par la tondeuse 1, la tondeuse 3 par les bords maximaux ; seules les cases occupées sont suivies
     * Résultat : positions finales attendues, code de sortie 0, sans erreur mémoire
     */
    @Test
    void run_lawnAtMaximumBounds_completesWithoutMaterializingLawn() throws Exception {
        int exitCode = cli.run(new String[]{fixture("maximum-bounds-lawn.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("0 2 N\n0 3 S\n40075016 40075016 N\n");
    }

    /**
     * Entrée : fichier dont une ligne est non conforme, après une tondeuse valide (orientation X en ligne 4),
     * sur la dernière ligne (instruction A en ligne 5), ou absente (ligne d'instructions attendue en ligne 3)
     * Scénario : le fichier est rejeté en entier avant toute exécution, seule l'erreur est écrite
     * Résultat : stdout vaut exactement "ERROR line <n>: <description>\n", aucune position, code de sortie 1
     */
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "invalid-orientation.txt      | ERROR line 4: invalid orientation 'X'",
            "invalid-instruction.txt      | ERROR line 5: invalid instruction 'A'",
            "missing-instruction-line.txt | ERROR line 3: missing instruction line"
    })
    void run_malformedLine_printsOnlyErrorWithLineNumber(String fixture, String expectedError) throws Exception {
        int exitCode = cli.run(new String[]{fixture(fixture)});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toByteArray()).isEqualTo((expectedError + "\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Entrée : exemple de référence écrit avec des fins de ligne Windows, en entier ou mélangées à des fins de ligne Unix,
     * avec ou sans saut de ligne final
     * Scénario : le \r qui précède chaque \n est retiré au découpage, un unique saut de ligne final n'est pas une ligne vide
     * Résultat : stdout vaut exactement "1 3 N\n5 1 E\n", comme pour le fichier Unix, code de sortie 0
     */
    @ParameterizedTest
    @MethodSource("referenceExampleWithWindowsOrMixedLineEndings")
    void run_referenceExampleWithWindowsOrMixedLineEndings_printsSameResultAsUnixFile(String content) throws Exception {
        int exitCode = cli.run(new String[]{generatedFile(utf8(content))});

        assertThat(exitCode).isZero();
        assertThat(stdout.toByteArray()).isEqualTo("1 3 N\n5 1 E\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Entrée : fichier dont la structure des lignes est invalide : ligne vide au milieu, second saut de ligne final,
     * \r isolé, BOM UTF-8 en tête
     * Scénario : le fichier est rejeté en entier, le caractère de contrôle cité dans le message est échappé
     * Résultat : stdout vaut exactement "ERROR line <n>: <description>\n" sur une seule ligne, code de sortie 1
     */
    @ParameterizedTest
    @MethodSource("filesWithInvalidLineStructure")
    void run_invalidLineStructure_printsOnlyErrorWithLineNumber(byte[] content, String expectedError) throws Exception {
        int exitCode = cli.run(new String[]{generatedFile(content)});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toByteArray()).isEqualTo((expectedError + "\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Entrée : fichier bien formé qui décrit une pelouse impossible ou des tondeuses mal placées : pelouse 0 0,
     * dimension au-delà de 40075016, coordonnée de 20 chiffres, départ hors pelouse, deux tondeuses sur la même case
     * Scénario : le fichier est rejeté en entier avant toute exécution, sans exception technique de dépassement
     * Résultat : stdout vaut exactement "ERROR line <n>: <description>\n", code de sortie 1
     */
    @ParameterizedTest
    @MethodSource("inconsistentPlans")
    void run_inconsistentPlan_printsOnlyErrorWithLineNumber(String content, String expectedError) throws Exception {
        int exitCode = cli.run(new String[]{generatedFile(utf8(content))});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo(expectedError + "\n");
    }

    /**
     * Entrée : appel sans argument, ou avec deux arguments
     * Scénario : la CLI attend exactement un chemin de fichier et ne lit jamais l'entrée standard
     * Résultat : stdout vaut exactement "ERROR: usage: mower <file>\n", code de sortie 2
     */
    @ParameterizedTest
    @MethodSource("invalidArguments")
    void run_wrongNumberOfArguments_printsUsageWithExitCodeTwo(String[] args) throws Exception {
        int exitCode = cli.run(args);

        assertThat(exitCode).isEqualTo(2);
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("ERROR: usage: mower <file>\n");
    }

    /**
     * Entrée : chemin absent, dossier, fichier vide, fichier de 10000001 octets, fichier non UTF-8
     * Scénario : l'erreur porte sur le fichier entier, elle est détectée avant toute analyse des lignes
     * Résultat : stdout vaut exactement "ERROR: <description>\n", sans numéro de ligne, code de sortie 1
     */
    @ParameterizedTest
    @MethodSource("unusableFiles")
    void run_unusableFile_printsWholeFileErrorWithoutLineNumber(UnusableFile unusableFile) throws Exception {
        Path input = unusableFile.create(this);

        int exitCode = cli.run(new String[]{input.toString()});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("ERROR: " + unusableFile.expectedDescription(input) + "\n");
    }

    private static Stream<UnusableFile> unusableFiles() {
        return Stream.of(
                new UnusableFile("missing", test -> test.tempDir.resolve("missing.txt"), path -> "file not found: " + path),
                new UnusableFile("directory", test -> test.tempDir, path -> "path is a directory: " + path),
                new UnusableFile("empty", test -> test.generatedPath(new byte[0]), path -> "file is empty"),
                new UnusableFile("too large", test -> test.generatedPath(new byte[10_000_001]), path -> "file size 10000001 bytes exceeds limit of 10000000 bytes"),
                new UnusableFile("not UTF-8", test -> test.generatedPath(new byte[]{'5', ' ', '5', '\n', (byte) 0xFF}), path -> "file is not valid UTF-8")
        );
    }

    private record UnusableFile(String name, FileFactory factory, Function<Path, String> description) {

        Path create(MowerCliIntegrationTest test) throws IOException {
            return factory.create(test);
        }

        String expectedDescription(Path path) {
            return description.apply(path);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FunctionalInterface
    private interface FileFactory {
        Path create(MowerCliIntegrationTest test) throws IOException;
    }

    /**
     * Entrée : argument qui n'est pas un chemin valide pour le système (caractère NUL)
     * Scénario : le chemin est traité comme un fichier absent, le caractère invisible est échappé dans le message
     * Résultat : stdout vaut exactement "ERROR: file not found: bad\\u0000path\n", code de sortie 1
     */
    @Test
    void run_invalidPathArgument_printsFileNotFound() throws Exception {
        int exitCode = cli.run(new String[]{"bad\u0000path"});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("ERROR: file not found: bad\\u0000path\n");
    }

    private static Stream<Arguments> invalidArguments() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"a.txt", "b.txt"})
        );
    }

    private static Stream<Arguments> inconsistentPlans() {
        return Stream.of(
                Arguments.of("0 0\n0 0 N\nF\n", "ERROR line 1: lawn 0 0 must contain more than one cell"),
                Arguments.of("40075017 5\n1 2 N\nF\n", "ERROR line 1: lawn dimension 40075017 must be between 0 and 40075016"),
                Arguments.of("99999999999999999999 5\n1 2 N\nF\n", "ERROR line 1: invalid coordinate '99999999999999999999'"),
                Arguments.of("5 5\n6 2 N\nF\n", "ERROR line 2: starting position 6 2 is outside the lawn"),
                Arguments.of("5 5\n1 2 N\nF\n1 2 E\nF\n", "ERROR line 4: starting position 1 2 is already taken")
        );
    }

    private static Stream<String> referenceExampleWithWindowsOrMixedLineEndings() {
        return Stream.of(
                "5 5\r\n1 2 N\r\nLFLFLFLFF\r\n3 3 E\r\nFFRFFRFRRF\r\n",
                "5 5\r\n1 2 N\nLFLFLFLFF\r\n3 3 E\nFFRFFRFRRF\n",
                "5 5\r\n1 2 N\r\nLFLFLFLFF\r\n3 3 E\r\nFFRFFRFRRF"
        );
    }

    private static Stream<Arguments> filesWithInvalidLineStructure() {
        byte[] byteOrderMark = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        return Stream.of(
                Arguments.of(utf8("5 5\n1 2 N\n\nF\n"), "ERROR line 3: empty line"),
                Arguments.of(utf8("5 5\n1 2 N\nF\n\n"), "ERROR line 4: empty line"),
                Arguments.of(utf8("5 5\r\n1 2 N\r\r\nF\r\n"), "ERROR line 2: invalid character '\\r'"),
                Arguments.of(utf8("5 5\r1 2 N\nF\n"), "ERROR line 1: invalid character '\\r'"),
                Arguments.of(concat(byteOrderMark, utf8("5 5\n1 2 N\nF\n")), "ERROR line 1: unexpected byte order mark")
        );
    }

    private Path generatedPath(byte[] content) throws IOException {
        return Path.of(generatedFile(content));
    }

    private String generatedFile(byte[] content) throws IOException {
        Path file = Files.createTempFile(tempDir, "input", ".txt");
        Files.write(file, content);
        return file.toString();
    }

    private static byte[] utf8(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private MowerCli newCli() {
        MowerOutputWriter mowerOutputWriter = new MowerOutputWriter(stdout);
        return new MowerCli(new MowLawnUseCase(new MowingPlanFileReader(), mowerOutputWriter), mowerOutputWriter);
    }

    private static String fixture(String name) throws URISyntaxException, IOException {
        URL resource = MowerCliIntegrationTest.class.getResource("/inputs/" + name);
        if (resource == null) {
            throw new IOException("Missing fixture " + name);
        }
        return Path.of(resource.toURI()).toString();
    }
}
