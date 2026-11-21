package com.automower.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    private static final int GRID_SIDE = 1000;

    @Test
    void shouldHaveSameHash_whenPositionsAreEqual() {
        assertThat(new Position(40075016, 3)).hasSameHashCodeAs(new Position(40075016, 3));
    }

    @Test
    void shouldSpreadHashes_whenPositionsFormDenseGrid() {
        Set<Integer> hashes = new HashSet<>();
        for (int x = 0; x < GRID_SIDE; x++) {
            for (int y = 0; y < GRID_SIDE; y++) {
                hashes.add(new Position(x, y).hashCode());
            }
        }

        // le hash par défaut d'un record ne donnerait ici que ~32 000 valeurs distinctes
        assertThat(hashes).hasSizeGreaterThan(GRID_SIDE * GRID_SIDE * 99 / 100);
    }
}
