package com.automower.infrastructure.cli;

import com.automower.domain.Mower;
import com.automower.domain.exception.BusinessValidationException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class MowerOutputWriter {

    private static final char LINE_END = '\n';

    // tampon vidé une seule fois en fin d'exécution : jusqu'à 10⁶ lignes sans un appel système par ligne
    private final Writer stdout;

    public MowerOutputWriter(OutputStream stdout) {
        this.stdout = new BufferedWriter(new OutputStreamWriter(stdout, StandardCharsets.UTF_8));
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

    public void flush() throws IOException {
        stdout.flush();
    }

    private void writeLine(String line) throws IOException {
        stdout.write(line);
        stdout.write(LINE_END);
    }
}
