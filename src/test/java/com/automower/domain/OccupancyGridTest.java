package com.automower.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OccupancyGridTest {

    private static final Lawn LAWN = new Lawn(5, 3);
    private static final Position CELL = new Position(2, 2);

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1", "6, 0", "0, 4"})
    void shouldNotBeFree_whenCellIsOutOfLawn(int x, int y) {
        assertThat(new OccupancyGrid(LAWN, List.of()).isFree(new Position(x, y))).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "5, 3", "2, 2"})
    void shouldBeFree_whenCellIsInLawnAndNotOccupied(int x, int y) {
        assertThat(new OccupancyGrid(LAWN, List.of()).isFree(new Position(x, y))).isTrue();
    }

    @Test
    void shouldNotBeFree_whenCellIsInitiallyOccupied() {
        assertThat(new OccupancyGrid(LAWN, List.of(CELL)).isFree(CELL)).isFalse();
    }

    @Test
    void shouldBeFree_whenOccupantLeftCell() {
        OccupancyGrid grid = new OccupancyGrid(LAWN, List.of(CELL));

        grid.leave(CELL);

        assertThat(grid.isFree(CELL)).isTrue();
    }

    @Test
    void shouldNotBeFree_whenCellBecameOccupied() {
        OccupancyGrid grid = new OccupancyGrid(LAWN, List.of());

        grid.occupy(CELL);

        assertThat(grid.isFree(CELL)).isFalse();
    }
}
