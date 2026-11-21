package com.automower.application;

import com.automower.domain.Mower;
import com.automower.domain.MowingPlan;
import com.automower.infrastructure.cli.MowerOutputWriter;
import com.automower.infrastructure.file.MowingPlanFileReader;

import java.io.IOException;
import java.nio.file.Path;

public class MowLawnUseCase {

    private final MowingPlanFileReader mowingPlanFileReader;
    private final MowerOutputWriter mowerOutputWriter;

    public MowLawnUseCase(MowingPlanFileReader mowingPlanFileReader, MowerOutputWriter mowerOutputWriter) {
        this.mowingPlanFileReader = mowingPlanFileReader;
        this.mowerOutputWriter = mowerOutputWriter;
    }

    public void mow(Path inputFile) throws IOException {
        MowingPlan plan = mowingPlanFileReader.read(inputFile);
        Mower finalMower = plan.run();
        mowerOutputWriter.write(finalMower);
    }
}
