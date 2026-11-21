package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class OrientationTest {

    @ParameterizedTest
    @CsvSource({"N, W", "W, S", "S, E", "E, N"})
    void shouldTurnCounterClockwise_whenTurningLeft(String from, String expected) {
        assertThat(Orientation.fromCode(from).left()).isEqualTo(Orientation.fromCode(expected));
    }

    @ParameterizedTest
    @CsvSource({"N, E", "E, S", "S, W", "W, N"})
    void shouldTurnClockwise_whenTurningRight(String from, String expected) {
        assertThat(Orientation.fromCode(from).right()).isEqualTo(Orientation.fromCode(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = {"n", "X", "NE", ""})
    void shouldRejectWithInvalidOrientationCode_whenCodeIsUnknown(String code) {
        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> Orientation.fromCode(code));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_ORIENTATION);
        assertThat(exception.getTitle()).isEqualTo("Invalid orientation");
        assertThat(exception).hasMessage("invalid orientation '" + code + "'");
    }
}
