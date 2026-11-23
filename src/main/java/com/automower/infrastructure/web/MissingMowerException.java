package com.automower.infrastructure.web;

public class MissingMowerException extends RuntimeException {

    public MissingMowerException() {
        super("at least one mower is required");
    }
}
