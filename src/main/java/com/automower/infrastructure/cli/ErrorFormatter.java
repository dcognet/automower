package com.automower.infrastructure.cli;

import com.automower.domain.exception.BusinessValidationException;

public final class ErrorFormatter {

    private static final String PREFIX = "ERROR";

    private ErrorFormatter() {
        // vide
    }

    public static String format(BusinessValidationException exception) {
        String location = exception.getLineNumber().isPresent() ? " line " + exception.getLineNumber().getAsInt() : "";
        return PREFIX + location + ": " + escapeControlCharacters(exception.getMessage());
    }

    // un caractère de contrôle cité dans le message est échappé pour garder une seule ligne lisible
    private static String escapeControlCharacters(String message) {
        StringBuilder escaped = new StringBuilder(message.length());
        message.chars().forEach(character -> escaped.append(escape((char) character)));
        return escaped.toString();
    }

    private static String escape(char character) {
        return switch (character) {
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
