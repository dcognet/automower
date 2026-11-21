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
}
