package com.automower.infrastructure.cli;

import com.automower.application.MowLawnUseCase;
import com.automower.domain.Mower;
import com.automower.infrastructure.file.InputFileReader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class MowerCli {

    private static final int EXIT_SUCCESS = 0;
    private static final String LINE_END = "\n";

    private final InputFileReader inputFileReader;
    private final MowLawnUseCase mowLawnUseCase;

    public MowerCli(InputFileReader inputFileReader, MowLawnUseCase mowLawnUseCase) {
        this.inputFileReader = inputFileReader;
        this.mowLawnUseCase = mowLawnUseCase;
    }

    public static void main(String[] args) throws IOException {
        MowerCli cli = new MowerCli(new InputFileReader(), new MowLawnUseCase());
        System.exit(cli.run(args, System.out));
    }

    public int run(String[] args, OutputStream stdout) throws IOException {
        List<String> lines = inputFileReader.readLines(Path.of(args[0]));
        Mower finalMower = mowLawnUseCase.mow(lines);
        Writer writer = new OutputStreamWriter(stdout, StandardCharsets.UTF_8);
        writer.write(MowerFormatter.format(finalMower) + LINE_END);
        writer.flush();
        return EXIT_SUCCESS;
    }
}
