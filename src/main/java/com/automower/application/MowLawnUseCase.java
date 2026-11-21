package com.automower.application;

import com.automower.domain.Mower;
import com.automower.domain.MowingPlan;
import com.automower.domain.MowingPlanParser;
import com.automower.infrastructure.cli.MowerOutputWriter;
import com.automower.infrastructure.file.InputFileReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MowLawnUseCase {

    private final InputFileReader inputFileReader;
    private final MowerOutputWriter mowerOutputWriter;

    public MowLawnUseCase(InputFileReader inputFileReader, MowerOutputWriter mowerOutputWriter) {
        this.inputFileReader = inputFileReader;
        this.mowerOutputWriter = mowerOutputWriter;
    }

    public void mow(Path inputFile) throws IOException {
        List<String> lines = inputFileReader.readLines(inputFile);
        MowingPlan plan = MowingPlanParser.parse(lines);
        Mower finalMower = plan.run();
        mowerOutputWriter.write(finalMower);
    }
}
