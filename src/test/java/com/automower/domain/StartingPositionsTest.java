package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class StartingPositionsTest {

    private final StartingPositions startingPositions = new StartingPositions(new Lawn(5, 3));

    @Test
    void shouldAcceptPositions_whenDistinctAndOnLawnIncludingCorners() {
        assertThatNoException().isThrownBy(() -> {
            startingPositions.add(new Position(0, 0));
            startingPositions.add(new Position(5, 3));
            startingPositions.add(new Position(1, 2));
            startingPositions.add(new Position(2, 1));
        });
    }

    @ParameterizedTest
    @CsvSource({"6, 2", "2, 4", "-1, 0", "0, -1"})
    void shouldRejectPosition_whenOutsideLawn(int x, int y) {
        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> startingPositions.add(new Position(x, y)));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.START_OUTSIDE_LAWN);
        assertThat(exception).hasMessage("starting position " + x + " " + y + " is outside the lawn");
    }

    @Test
    void shouldRejectSecondPosition_whenAlreadyTakenByPreviousMower() {
        startingPositions.add(new Position(1, 2));

        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> startingPositions.add(new Position(1, 2)));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.DUPLICATE_START);
        assertThat(exception).hasMessage("starting position 1 2 is already taken");
    }
}
