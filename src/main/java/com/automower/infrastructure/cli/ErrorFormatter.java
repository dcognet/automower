package com.automower.infrastructure.cli;

import com.automower.domain.exception.BusinessValidationException;

public final class ErrorFormatter {

    private static final String PREFIX = "ERROR";

    private ErrorFormatter() {
        // vide
    }

    public static String format(BusinessValidationException exception) {
        String location = exception.getLineNumber().isPresent() ? " line " + exception.getLineNumber().getAsInt() : "";
        return PREFIX + location + ": " + exception.getMessage();
    }
}
