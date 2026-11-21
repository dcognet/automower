package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class OrientationTest {

    @ParameterizedTest
    @CsvSource({"N, W", "W, S", "S, E", "E, N"})
    void shouldTurnCounterClockwise_whenTurningLeft(char from, char expected) {
        assertThat(Orientation.fromCode(from).left()).isEqualTo(Orientation.fromCode(expected));
    }

    @ParameterizedTest
    @CsvSource({"N, E", "E, S", "S, W", "W, N"})
    void shouldTurnClockwise_whenTurningRight(char from, char expected) {
        assertThat(Orientation.fromCode(from).right()).isEqualTo(Orientation.fromCode(expected));
    }

    @Test
    void shouldRejectWithInvalidOrientationCode_whenCodeIsUnknown() {
        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> Orientation.fromCode('n'));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_ORIENTATION);
        assertThat(exception.getTitle()).isEqualTo("Invalid orientation");
        assertThat(exception).hasMessage("invalid orientation 'n'");
    }
}
