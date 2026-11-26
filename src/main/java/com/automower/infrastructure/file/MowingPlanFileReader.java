package com.automower.infrastructure.file;

import com.automower.domain.MowingPlan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MowingPlanFileReader {

    static final int MAX_FILE_SIZE = 10_000_000;

    public MowingPlan read(Path path) {
        // contrôles sur le fichier entier, chacun bloquant le suivant : ils priment sur toute erreur de ligne
        requireReadableFile(path);
        requireSizeWithinLimit(path);
        byte[] content = readAtMostLimit(path);
        if (content.length == 0) {
            throw new InputFileException(InputFileErrorCode.EMPTY_FILE, "file is empty");
        }
        return MowingPlanFileMapper.toMowingPlan(LineSplitter.split(decodeUtf8(content)));
    }

    private static void requireReadableFile(Path path) {
        if (!Files.exists(path)) {
            throw new InputFileException(InputFileErrorCode.FILE_NOT_FOUND, "file not found: " + path);
        }
        if (Files.isDirectory(path)) {
            throw new InputFileException(InputFileErrorCode.FILE_NOT_READABLE, "path is a directory: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new InputFileException(InputFileErrorCode.FILE_NOT_READABLE, "file is not readable: " + path);
        }
    }

    private static void requireSizeWithinLimit(Path path) {
        long size;
        try {
            // métadonnées seulement : un fichier trop gros est rejeté sans lire son contenu
            size = Files.size(path);
        } catch (IOException exception) {
            throw notReadable(path);
        }
        if (size > MAX_FILE_SIZE) {
            throw new InputFileException(InputFileErrorCode.FILE_TOO_LARGE, "file size " + size + " bytes exceeds limit of " + MAX_FILE_SIZE + " bytes");
        }
    }

    private static byte[] readAtMostLimit(Path path) {
        byte[] content;
        // lecture plafonnée : la taille annoncée peut être fausse (fichier qui grossit, pipe nommé, /proc)
        try (InputStream input = Files.newInputStream(path)) {
            content = input.readNBytes(MAX_FILE_SIZE + 1);
        } catch (IOException exception) {
            throw notReadable(path);
        }
        if (content.length > MAX_FILE_SIZE) {
            throw new InputFileException(InputFileErrorCode.FILE_TOO_LARGE, "file size exceeds limit of " + MAX_FILE_SIZE + " bytes");
        }
        return content;
    }

    private static String decodeUtf8(byte[] content) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new InputFileException(InputFileErrorCode.INVALID_ENCODING, "file is not valid UTF-8");
        }
    }

    private static InputFileException notReadable(Path path) {
        return new InputFileException(InputFileErrorCode.FILE_NOT_READABLE, "file is not readable: " + path);
    }
}
