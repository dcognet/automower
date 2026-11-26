package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class InstructionTest {

    @ParameterizedTest
    @CsvSource({"L, LEFT", "R, RIGHT", "F, FORWARD"})
    void shouldMapCodeToInstruction_whenCodeIsKnown(String code, Instruction expected) {
        assertThat(Instruction.fromCode(code)).isEqualTo(expected);
    }

    @Test
    void shouldRejectWithInvalidInstructionCode_whenCodeIsUnknown() {
        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> Instruction.fromCode("A"));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_INSTRUCTION);
        assertThat(exception.getTitle()).isEqualTo("Invalid instruction");
        assertThat(exception).hasMessage("invalid instruction 'A'");
    }
}
