package com.automower.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrientationTest {

    @ParameterizedTest
    @CsvSource({"N, W", "W, S", "S, E", "E, N"})
    void shouldTurnCounterClockwise_whenTurningLeft(char from, char expected) {
        assertThat(Orientation.fromCode(from).left()).isEqualTo(Orientation.fromCode(expected));
    }

    @ParameterizedTest
    @CsvSource({"N, E", "E, S", "S, W", "W, N"})
    void shouldTurnClockwise_whenTurningRight(char from, char expected) {
        assertThat(Orientation.fromCode(from).right()).isEqualTo(Orientation.fromCode(expected));
    }
}
