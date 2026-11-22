package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class LawnTest {

    @ParameterizedTest
    @CsvSource({"5, 0", "0, 3", "40075016, 0", "0, 40075016", "40075016, 40075016"})
    void shouldAcceptLawn_whenDimensionsAreWithinBoundsAndLawnHasSeveralCells(int maxX, int maxY) {
        assertThatNoException().isThrownBy(() -> new Lawn(maxX, maxY));
    }

    @Test
    void shouldRejectLawn_whenLawnHasSingleCell() {
        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> new Lawn(0, 0));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.SINGLE_CELL_LAWN);
        assertThat(exception).hasMessage("lawn 0 0 must contain more than one cell");
    }

    @ParameterizedTest
    @CsvSource({"40075017, 5, 40075017", "5, 40075017, 40075017", "-1, 5, -1", "5, -1, -1"})
    void shouldRejectLawn_whenDimensionIsOutOfBounds(int maxX, int maxY, int invalidDimension) {
        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> new Lawn(maxX, maxY));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_LAWN_DIMENSION);
        assertThat(exception).hasMessage("lawn dimension " + invalidDimension + " must be between 0 and 40075016");
    }
}
