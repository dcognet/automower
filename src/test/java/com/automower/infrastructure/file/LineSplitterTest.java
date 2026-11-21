package com.automower.infrastructure.file;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineSplitterTest {

    @Test
    void shouldNotProduceEmptyLine_whenTextEndsWithSingleLineFeed() {
        assertThat(LineSplitter.split("5 5\n1 2 N\nF\n")).containsExactly("5 5", "1 2 N", "F");
    }

    @Test
    void shouldKeepLastLine_whenTextHasNoFinalLineFeed() {
        assertThat(LineSplitter.split("5 5\n1 2 N\nF")).containsExactly("5 5", "1 2 N", "F");
    }

    @Test
    void shouldStripCarriageReturn_whenLineEndsWithCrLf() {
        assertThat(LineSplitter.split("5 5\r\n1 2 N\nF\r\n")).containsExactly("5 5", "1 2 N", "F");
    }

    @Test
    void shouldAcceptBothLineEndings_whenMixedInSameText() {
        assertThat(LineSplitter.split("5 5\r\n1 2 N\nF\r\n3 3 E\nL")).containsExactly("5 5", "1 2 N", "F", "3 3 E", "L");
    }

    @Test
    void shouldProduceEmptyLine_whenTextContainsEmptyLineOrSecondFinalLineFeed() {
        assertThat(LineSplitter.split("5 5\n\n1 2 N\r\n\r\n")).containsExactly("5 5", "", "1 2 N", "");
    }

    @Test
    void shouldKeepCarriageReturnInLine_whenNotFollowedByLineFeed() {
        assertThat(LineSplitter.split("5 5\r1 2 N\r\r\nF\r")).containsExactly("5 5\r1 2 N\r", "F\r");
    }
}
