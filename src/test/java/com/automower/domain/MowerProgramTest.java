package com.automower.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void shouldUseIndependentBounds_whenLawnIsRectangular() {
        MowerProgram program = new MowerProgram(new Mower(new Position(0, 0), Orientation.EAST), "FFFFFFL");

        assertThat(program.runOn(new Lawn(5, 3))).isEqualTo(new Mower(new Position(5, 0), Orientation.NORTH));
    }
}
