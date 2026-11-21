package com.automower.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class MowerTest {

    private static final Lawn LAWN = new Lawn(5, 5);
    private static final Position CENTER = new Position(2, 2);

    @ParameterizedTest
    @EnumSource(value = Instruction.class, names = {"LEFT", "RIGHT"})
    void shouldKeepPosition_whenRotating(Instruction instruction) {
        Mower mower = new Mower(CENTER, Orientation.NORTH);

        assertThat(mower.execute(instruction, LAWN).position()).isEqualTo(CENTER);
    }

    @ParameterizedTest
    @CsvSource({"N, 2, 3", "E, 3, 2", "S, 2, 1", "W, 1, 2"})
    void shouldMoveOneCellFacingDirection_whenMovingForward(char orientation, int expectedX, int expectedY) {
        Mower mower = new Mower(CENTER, Orientation.fromCode(orientation));

        Mower moved = mower.execute(Instruction.FORWARD, LAWN);

        assertThat(moved).isEqualTo(new Mower(new Position(expectedX, expectedY), Orientation.fromCode(orientation)));
    }

    @ParameterizedTest
    @CsvSource({"0, 3, W", "5, 3, E", "3, 0, S", "3, 5, N"})
    void shouldStayStill_whenMovingForwardOutOfLawn(int x, int y, char orientation) {
        Mower mower = new Mower(new Position(x, y), Orientation.fromCode(orientation));

        assertThat(mower.execute(Instruction.FORWARD, LAWN)).isEqualTo(mower);
    }

    @Test
    void shouldReachUpperRightCorner_whenBoundsAreInclusive() {
        Mower mower = new Mower(new Position(4, 5), Orientation.EAST);

        assertThat(mower.execute(Instruction.FORWARD, LAWN).position()).isEqualTo(new Position(5, 5));
    }
}
