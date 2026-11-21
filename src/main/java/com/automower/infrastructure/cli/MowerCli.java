package com.automower.infrastructure.cli;

import com.automower.application.MowLawnUseCase;
import com.automower.domain.exception.BusinessValidationException;
import com.automower.infrastructure.file.MowingPlanFileReader;

import java.io.IOException;
import java.nio.file.Path;

public class MowerCli {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_ERROR = 1;

    private final MowLawnUseCase mowLawnUseCase;
    private final MowerOutputWriter mowerOutputWriter;

    public MowerCli(MowLawnUseCase mowLawnUseCase, MowerOutputWriter mowerOutputWriter) {
        this.mowLawnUseCase = mowLawnUseCase;
        this.mowerOutputWriter = mowerOutputWriter;
    }

    public static void main(String[] args) throws IOException {
        MowerOutputWriter mowerOutputWriter = new MowerOutputWriter(System.out);
        MowLawnUseCase mowLawnUseCase = new MowLawnUseCase(new MowingPlanFileReader(), mowerOutputWriter);
        System.exit(new MowerCli(mowLawnUseCase, mowerOutputWriter).run(args));
    }

    public int run(String[] args) throws IOException {
        try {
            mowLawnUseCase.mow(Path.of(args[0]));
            return EXIT_SUCCESS;
        } catch (BusinessValidationException exception) {
            // le plan est validé en entier avant la simulation : aucune position n'a été écrite
            mowerOutputWriter.writeError(exception);
            return EXIT_ERROR;
        }
    }
}
