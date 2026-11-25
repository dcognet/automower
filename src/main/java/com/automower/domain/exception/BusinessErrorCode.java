package com.automower.domain.exception;

public enum BusinessErrorCode {
    INVALID_LAWN_DIMENSION("Invalid lawn dimension"),
    SINGLE_CELL_LAWN("Single cell lawn"),
    START_OUTSIDE_LAWN("Starting position outside the lawn"),
    DUPLICATE_START("Duplicate starting position"),
    INVALID_ORIENTATION("Invalid orientation"),
    INVALID_INSTRUCTION("Invalid instruction"),
    EMPTY_INSTRUCTIONS("Empty instruction sequence");

    private final String title;

    BusinessErrorCode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
