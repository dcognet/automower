package com.automower.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MowingPlanParserTest {

    @Test
    void shouldBuildLawnMowerAndInstructions_whenLinesAreConforming() {
        MowingPlan plan = MowingPlanParser.parse(List.of("10 12", "7 11 W", "LFRF"));

        MowerProgram expectedProgram = new MowerProgram(new Mower(new Position(7, 11), Orientation.WEST), "LFRF");
        assertThat(plan).isEqualTo(new MowingPlan(new Lawn(10, 12), expectedProgram));
    }
}
