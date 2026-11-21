package com.automower.infrastructure.cli;

import com.automower.application.MowLawnUseCase;
import com.automower.infrastructure.file.MowingPlanFileReader;

import java.io.IOException;
import java.nio.file.Path;

public class MowerCli {

    private static final int EXIT_SUCCESS = 0;

    private final MowLawnUseCase mowLawnUseCase;

    public MowerCli(MowLawnUseCase mowLawnUseCase) {
        this.mowLawnUseCase = mowLawnUseCase;
    }

    public static void main(String[] args) throws IOException {
        MowLawnUseCase mowLawnUseCase = new MowLawnUseCase(new MowingPlanFileReader(), new MowerOutputWriter(System.out));
        System.exit(new MowerCli(mowLawnUseCase).run(args));
    }

    public int run(String[] args) throws IOException {
        mowLawnUseCase.mow(Path.of(args[0]));
        return EXIT_SUCCESS;
    }
}
