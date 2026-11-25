package com.automower.infrastructure.file;

public enum InputFileErrorCode {
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
    MISSING_MOWER("Missing mower"),
    MISSING_INSTRUCTIONS("Missing instruction line");

    private final String title;

    InputFileErrorCode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
