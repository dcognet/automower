package com.automower.infrastructure.cli;

import com.automower.domain.Mower;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class MowerOutputWriter {

    private static final String LINE_END = "\n";

    private final OutputStream stdout;

    public MowerOutputWriter(OutputStream stdout) {
        this.stdout = stdout;
    }

    public void write(Mower mower) throws IOException {
        Writer writer = new OutputStreamWriter(stdout, StandardCharsets.UTF_8);
        writer.write(MowerFormatter.format(mower) + LINE_END);
        writer.flush();
    }
}
