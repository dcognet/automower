package com.automower.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MowingPlanTest {

    @Test
    void shouldReturnOneFinalMowerPerProgramInFileOrder_whenPlanHasSeveralMowers() {
        MowerProgram first = new MowerProgram(new Mower(new Position(1, 2), Orientation.NORTH), "LFLFLFLFF");
        MowerProgram second = new MowerProgram(new Mower(new Position(3, 3), Orientation.EAST), "FFRFFRFRRF");

        List<Mower> finalMowers = new MowingPlan(new Lawn(5, 5), List.of(first, second)).run();

        assertThat(finalMowers).containsExactly(
                new Mower(new Position(1, 3), Orientation.NORTH),
                new Mower(new Position(5, 1), Orientation.EAST)
        );
    }
}
