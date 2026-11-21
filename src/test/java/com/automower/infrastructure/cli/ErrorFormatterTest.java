package com.automower.infrastructure.cli;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorFormatterTest {

    @ParameterizedTest
    @MethodSource("quotedCharacters")
    void shouldEscapeOnlyInvisibleCharacters_whenMessageQuotesThem(String quoted, String expectedQuoted) {
        BusinessValidationException exception = new BusinessValidationException(BusinessErrorCode.INVALID_CHARACTER, "invalid value '" + quoted + "'");

        assertThat(ErrorFormatter.format(exception.atLine(2))).isEqualTo("ERROR line 2: invalid value '" + expectedQuoted + "'");
    }

    private static Stream<Arguments> quotedCharacters() {
        return Stream.of(
                Arguments.of("\r", "\\r"),
                Arguments.of("5\t5", "5\\t5"),
                Arguments.of("N\n", "N\\n"),
                Arguments.of("﻿5", "\\uFEFF5"),
                Arguments.of("\u0000", "\\u0000"),
                Arguments.of("\u0085", "\\u0085"),
                Arguments.of(" ", "\\u2028"),
                Arguments.of(" ", "\\u2029"),
                Arguments.of("٥", "٥"),
                Arguments.of("é", "é"),
                Arguments.of("5 5", "5 5")
        );
    }
}
