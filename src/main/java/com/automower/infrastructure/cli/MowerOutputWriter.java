package com.automower.infrastructure.cli;

import com.automower.domain.Mower;
import com.automower.domain.exception.BusinessValidationException;

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
        writeLine(MowerFormatter.format(mower));
    }

    public void writeError(BusinessValidationException exception) throws IOException {
        writeLine(ErrorFormatter.format(exception));
    }

    public void writeError(String description) throws IOException {
        writeLine(ErrorFormatter.format(description));
    }

    private void writeLine(String line) throws IOException {
        Writer writer = new OutputStreamWriter(stdout, StandardCharsets.UTF_8);
        writer.write(line + LINE_END);
        writer.flush();
    }
}
