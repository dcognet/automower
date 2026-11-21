package com.automower.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessValidationExceptionTest {

    @Test
    void shouldHaveNoLineNumber_whenCreated() {
        BusinessValidationException exception = new BusinessValidationException(BusinessErrorCode.INVALID_INSTRUCTION, "invalid instruction 'A'");

        assertThat(exception.getLineNumber()).isEmpty();
    }

    @Test
    void shouldKeepCodeAndDetailAndAddLineNumber_whenLocatedAtLine() {
        BusinessValidationException exception = new BusinessValidationException(BusinessErrorCode.INVALID_ORIENTATION, "invalid orientation 'X'").atLine(4);

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_ORIENTATION);
        assertThat(exception).hasMessage("invalid orientation 'X'");
        assertThat(exception.getLineNumber()).hasValue(4);
    }
}
