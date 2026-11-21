package com.automower.infrastructure.file;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MowingPlanFileMapperTest {

    @Test
    void shouldBuildLawnMowerAndInstructions_whenLinesAreConforming() {
        MowingPlan plan = MowingPlanFileMapper.toMowingPlan(List.of("10 12", "7 11 W", "LFRF"));

        MowerProgram expectedProgram = new MowerProgram(new Mower(new Position(7, 11), Orientation.WEST), "LFRF");
        assertThat(plan).isEqualTo(new MowingPlan(new Lawn(10, 12), expectedProgram));
    }
}
