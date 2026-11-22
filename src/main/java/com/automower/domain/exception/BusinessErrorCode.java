package com.automower.domain.exception;

public enum BusinessErrorCode {
    FILE_NOT_FOUND("File not found"),
    FILE_NOT_READABLE("File not readable"),
    FILE_TOO_LARGE("File too large"),
    EMPTY_FILE("Empty file"),
    INVALID_ENCODING("Invalid encoding"),
    EMPTY_LINE("Empty line"),
    INVALID_CHARACTER("Invalid character"),
    UNEXPECTED_BYTE_ORDER_MARK("Unexpected byte order mark"),
    INVALID_LAWN_LINE("Invalid lawn line"),
    INVALID_POSITION_LINE("Invalid position line"),
    INVALID_COORDINATE("Invalid coordinate"),
    INVALID_LAWN_DIMENSION("Invalid lawn dimension"),
    SINGLE_CELL_LAWN("Single cell lawn"),
    START_OUTSIDE_LAWN("Starting position outside the lawn"),
    DUPLICATE_START("Duplicate starting position"),
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
