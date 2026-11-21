package com.automower.domain.exception;

import java.util.OptionalInt;

public class BusinessValidationException extends RuntimeException {

    private final BusinessErrorCode code;
    private final OptionalInt lineNumber;

    public BusinessValidationException(BusinessErrorCode code, String detail) {
        this(code, detail, OptionalInt.empty());
    }

    private BusinessValidationException(BusinessErrorCode code, String detail, OptionalInt lineNumber) {
        super(detail);
        this.code = code;
        this.lineNumber = lineNumber;
    }

    public BusinessValidationException atLine(int lineNumber) {
        return new BusinessValidationException(code, getMessage(), OptionalInt.of(lineNumber));
    }

    public BusinessErrorCode getCode() {
        return code;
    }

    public String getTitle() {
        return code.getTitle();
    }

    public OptionalInt getLineNumber() {
        return lineNumber;
    }
}
