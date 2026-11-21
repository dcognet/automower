package com.automower.infrastructure.cli;

import com.automower.domain.exception.BusinessValidationException;

import java.util.stream.Collectors;

public final class ErrorFormatter {

    private static final String PREFIX = "ERROR";

    private ErrorFormatter() {
        // vide
    }

    public static String format(BusinessValidationException exception) {
        String location = exception.getLineNumber().isPresent() ? " line " + exception.getLineNumber().getAsInt() : "";
        return PREFIX + location + ": " + escapeInvisibleCharacters(exception.getMessage());
    }

    // un caractère invisible cité dans le message est échappé pour garder une seule ligne lisible
    private static String escapeInvisibleCharacters(String message) {
        return message.chars()
                .mapToObj(character -> escape((char) character))
                .collect(Collectors.joining());
    }

    private static String escape(char character) {
        return switch (character) {
            // le backslash est doublé : un \r échappé ne se confond pas avec un backslash suivi de r
            case '\\' -> "\\\\";
            case '\t' -> "\\t";
            case '\r' -> "\\r";
            case '\n' -> "\\n";
            default -> isInvisible(character) ? String.format("\\u%04X", (int) character) : String.valueOf(character);
        };
    }

    private static boolean isInvisible(char character) {
        int type = Character.getType(character);
        return Character.isISOControl(character)
                || type == Character.FORMAT
                || type == Character.LINE_SEPARATOR
                || type == Character.PARAGRAPH_SEPARATOR;
    }
}
