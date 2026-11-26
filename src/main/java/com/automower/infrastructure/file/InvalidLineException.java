package com.automower.infrastructure.file;

import com.automower.domain.exception.BusinessValidationException;

public class InvalidLineException extends RuntimeException {

    private final int lineNumber;

    // la cause dit ce qui ne va pas (format du fichier ou règle métier), cette exception dit où
    public InvalidLineException(int lineNumber, InputFileException cause) {
        super(cause.getMessage(), cause);
        this.lineNumber = lineNumber;
    }

    public InvalidLineException(int lineNumber, BusinessValidationException cause) {
        super(cause.getMessage(), cause);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
