package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

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

    @Test
    void shouldBlockFollowingMower_whenPreviousMowerStoppedOnTargetCell() {
        MowerProgram first = new MowerProgram(new Mower(new Position(1, 1), Orientation.NORTH), "L");
        MowerProgram second = new MowerProgram(new Mower(new Position(0, 1), Orientation.EAST), "FL");

        List<Mower> finalMowers = new MowingPlan(new Lawn(5, 5), List.of(first, second)).run();

        assertThat(finalMowers).containsExactly(
                new Mower(new Position(1, 1), Orientation.WEST),
                new Mower(new Position(0, 1), Orientation.NORTH)
        );
    }

    @Test
    void shouldBlockPreviousMower_whenFollowingMowerHasNotStartedYet() {
        MowerProgram first = new MowerProgram(new Mower(new Position(0, 0), Orientation.EAST), "FF");
        MowerProgram second = new MowerProgram(new Mower(new Position(2, 0), Orientation.NORTH), "L");

        List<Mower> finalMowers = new MowingPlan(new Lawn(5, 5), List.of(first, second)).run();

        assertThat(finalMowers).containsExactly(
                new Mower(new Position(1, 0), Orientation.EAST),
                new Mower(new Position(2, 0), Orientation.WEST)
        );
    }

    @Test
    void shouldLetMowerComeBackToItsStart_whenItsOwnStartIsTheOnlyOccupiedCell() {
        MowerProgram program = new MowerProgram(new Mower(new Position(0, 0), Orientation.NORTH), "FRRF");

        assertThat(new MowingPlan(new Lawn(5, 5), List.of(program)).run()).containsExactly(new Mower(new Position(0, 0), Orientation.SOUTH));
    }

    @Test
    void shouldLetFollowingMowerCrossCell_whenPreviousMowerLeftItsStart() {
        MowerProgram first = new MowerProgram(new Mower(new Position(1, 0), Orientation.NORTH), "F");
        MowerProgram second = new MowerProgram(new Mower(new Position(0, 0), Orientation.EAST), "FF");

        List<Mower> finalMowers = new MowingPlan(new Lawn(5, 5), List.of(first, second)).run();

        assertThat(finalMowers).containsExactly(
                new Mower(new Position(1, 1), Orientation.NORTH),
                new Mower(new Position(2, 0), Orientation.EAST)
        );
    }

    @Test
    void shouldRejectPlan_whenStartingPositionIsOutsideLawn() {
        MowerProgram program = new MowerProgram(new Mower(new Position(6, 2), Orientation.NORTH), "F");

        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> new MowingPlan(new Lawn(5, 5), List.of(program)));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.START_OUTSIDE_LAWN);
    }

    @Test
    void shouldRejectPlan_whenTwoMowersStartOnSameCell() {
        MowerProgram first = new MowerProgram(new Mower(new Position(1, 2), Orientation.NORTH), "F");
        MowerProgram second = new MowerProgram(new Mower(new Position(1, 2), Orientation.EAST), "F");

        BusinessValidationException exception = catchThrowableOfType(BusinessValidationException.class, () -> new MowingPlan(new Lawn(5, 5), List.of(first, second)));

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.DUPLICATE_START);
    }
}
