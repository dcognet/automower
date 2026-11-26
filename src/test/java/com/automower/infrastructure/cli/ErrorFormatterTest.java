package com.automower.infrastructure.cli;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorFormatterTest {

    @ParameterizedTest
    @MethodSource("quotedCharacters")
    void shouldEscapeOnlyInvisibleCharacters_whenMessageQuotesThem(String quoted, String expectedQuoted) {
        assertThat(ErrorFormatter.format(2, "invalid value '" + quoted + "'")).isEqualTo("ERROR line 2: invalid value '" + expectedQuoted + "'");
    }

    private static Stream<Arguments> quotedCharacters() {
        return Stream.of(
                Arguments.of("\r", "\\r"),
                Arguments.of("5\t5", "5\\t5"),
                Arguments.of("N\n", "N\\n"),
                Arguments.of("\uFEFF5", "\\uFEFF5"),
                Arguments.of("\u0000", "\\u0000"),
                Arguments.of("\u0085", "\\u0085"),
                Arguments.of("\u2028", "\\u2028"),
                Arguments.of("\u2029", "\\u2029"),
                Arguments.of("a\\rb", "a\\\\rb"),
                Arguments.of("\u0665", "\u0665"),
                Arguments.of("\u00E9", "\u00E9"),
                Arguments.of("5\u00A05", "5\u00A05")
        );
    }
}
