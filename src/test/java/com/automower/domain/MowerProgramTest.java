package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class MowerProgramTest {

    @Test
    void shouldExecuteInstructionsInOrder_whenRunningSequence() {
        MowerProgram forwardThenRight = new MowerProgram(new Mower(new Position(0, 0), Orientation.NORTH), "FR");
        MowerProgram rightThenForward = new MowerProgram(new Mower(new Position(0, 0), Orientation.NORTH), "RF");

        Lawn lawn = new Lawn(5, 5);

        assertThat(forwardThenRight.runOn(lawn)).isEqualTo(new Mower(new Position(0, 1), Orientation.EAST));
        assertThat(rightThenForward.runOn(lawn)).isEqualTo(new Mower(new Position(1, 0), Orientation.EAST));
    }

    @Test
    void shouldContinueWithNextInstruction_whenForwardIsBlockedByEdge() {
        MowerProgram program = new MowerProgram(new Mower(new Position(5, 3), Orientation.EAST), "FL");

        assertThat(program.runOn(new Lawn(5, 5))).isEqualTo(new Mower(new Position(5, 3), Orientation.NORTH));
    }

    @ParameterizedTest
    @CsvSource({"E, FFFFFFL, 5, 0, N", "N, FFFFR, 0, 3, E"})
    void shouldUseIndependentBounds_whenLawnIsRectangular(
            char orientation,
            String instructions,
            int expectedX,
            int expectedY,
            char expectedOrientation
    ) {
        MowerProgram program = new MowerProgram(new Mower(new Position(0, 0), Orientation.fromCode(orientation)), instructions);

        Mower expected = new Mower(new Position(expectedX, expectedY), Orientation.fromCode(expectedOrientation));
        assertThat(program.runOn(new Lawn(5, 3))).isEqualTo(expected);
    }

    @Test
    void shouldRejectAtConstruction_whenSequenceContainsUnknownInstruction() {
        Mower mower = new Mower(new Position(0, 0), Orientation.NORTH);

        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> new MowerProgram(mower, "FLX"));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.INVALID_INSTRUCTION);
        assertThat(exception).hasMessage("invalid instruction 'X'");
    }
}
