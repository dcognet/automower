package com.automower.infrastructure.file;

public class InputFileException extends RuntimeException {

    private final InputFileErrorCode code;

    public InputFileException(InputFileErrorCode code, String detail) {
        super(detail);
        this.code = code;
    }

    public InputFileErrorCode getCode() {
        return code;
    }

    public String getTitle() {
        return code.getTitle();
    }
}
