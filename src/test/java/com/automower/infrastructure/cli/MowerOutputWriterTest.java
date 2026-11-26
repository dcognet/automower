package com.automower.infrastructure.cli;

import com.automower.domain.Mower;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MowerOutputWriterTest {

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final MowerOutputWriter mowerOutputWriter = new MowerOutputWriter(stdout);

    @Test
    void shouldWriteXYOrientationTerminatedByLineFeed_whenWritingMower() throws IOException {
        mowerOutputWriter.write(List.of(new Mower(new Position(12, 3), Orientation.WEST)));
        mowerOutputWriter.flush();

        assertThat(stdout.toByteArray()).isEqualTo("12 3 W\n".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldWriteOneLinePerMowerInListOrder_whenWritingSeveralMowers() throws IOException {
        mowerOutputWriter.write(List.of(
                new Mower(new Position(1, 3), Orientation.NORTH),
                new Mower(new Position(5, 1), Orientation.EAST)
        ));
        mowerOutputWriter.flush();

        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("1 3 N\n5 1 E\n");
    }

    @Test
    void shouldWriteErrorWithLineNumber_whenErrorIsLocatedAtLine() throws IOException {
        mowerOutputWriter.writeError(4, "invalid orientation 'X'");
        mowerOutputWriter.flush();

        assertThat(stdout.toByteArray()).isEqualTo("ERROR line 4: invalid orientation 'X'\n".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldWriteEscapedErrorWithoutLineNumber_whenWritingDescriptionOnly() throws IOException {
        mowerOutputWriter.writeError("file not found: a\tb.txt");
        mowerOutputWriter.flush();

        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("ERROR: file not found: a\\tb.txt\n");
    }

    @Test
    void shouldWriteNothing_whenNotFlushedYet() throws IOException {
        mowerOutputWriter.write(List.of(new Mower(new Position(1, 3), Orientation.NORTH)));

        assertThat(stdout.size()).isZero();
    }
}
