package com.automower.infrastructure.cli;

import com.automower.domain.Mower;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MowerOutputWriterTest {

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final MowerOutputWriter mowerOutputWriter = new MowerOutputWriter(stdout);

    @Test
    void shouldWriteXYOrientationTerminatedByLineFeed_whenWritingMower() throws IOException {
        mowerOutputWriter.write(new Mower(new Position(12, 3), Orientation.WEST));

        assertThat(stdout.toByteArray()).isEqualTo("12 3 W\n".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldWriteOneLinePerMowerInCallOrder_whenWritingSeveralMowers() throws IOException {
        mowerOutputWriter.write(new Mower(new Position(1, 3), Orientation.NORTH));
        mowerOutputWriter.write(new Mower(new Position(5, 1), Orientation.EAST));

        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("1 3 N\n5 1 E\n");
    }
}
