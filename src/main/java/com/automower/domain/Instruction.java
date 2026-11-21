package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;

public enum Instruction {
    LEFT,
    RIGHT,
    FORWARD;

    public static Instruction fromCode(char code) {
        return switch (code) {
            case 'L' -> LEFT;
            case 'R' -> RIGHT;
            case 'F' -> FORWARD;
            default -> throw new BusinessValidationException(BusinessErrorCode.INVALID_INSTRUCTION, "invalid instruction '" + code + "'");
        };
    }
}
