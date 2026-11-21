package com.automower.domain.exception;

public enum BusinessErrorCode {
    EMPTY_LINE("Empty line"),
    INVALID_CHARACTER("Invalid character"),
    UNEXPECTED_BYTE_ORDER_MARK("Unexpected byte order mark"),
    INVALID_LAWN_LINE("Invalid lawn line"),
    INVALID_POSITION_LINE("Invalid position line"),
    INVALID_COORDINATE("Invalid coordinate"),
    INVALID_ORIENTATION("Invalid orientation"),
    INVALID_INSTRUCTION("Invalid instruction"),
    EMPTY_INSTRUCTIONS("Empty instruction sequence"),
    MISSING_MOWER("Missing mower"),
    MISSING_INSTRUCTIONS("Missing instruction line");

    private final String title;

    BusinessErrorCode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
