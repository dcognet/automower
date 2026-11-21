package com.automower.domain.exception;

public class BusinessValidationException extends RuntimeException {

    private final BusinessErrorCode code;

    public BusinessValidationException(BusinessErrorCode code, String detail) {
        super(detail);
        this.code = code;
    }

    public BusinessErrorCode getCode() {
        return code;
    }

    public String getTitle() {
        return code.getTitle();
    }
}
