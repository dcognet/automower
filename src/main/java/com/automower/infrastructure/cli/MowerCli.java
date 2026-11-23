package com.automower.infrastructure.cli;

import com.automower.application.MowLawnUseCase;
import com.automower.domain.MowingPlan;
import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import com.automower.infrastructure.file.MowingPlanFileReader;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class MowerCli {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_ERROR = 1;
    private static final int EXIT_USAGE = 2;
    private static final String USAGE = "usage: mower <file>";

    private final MowingPlanFileReader mowingPlanFileReader;
    private final MowLawnUseCase mowLawnUseCase;
    private final MowerOutputWriter mowerOutputWriter;

    public MowerCli(MowingPlanFileReader mowingPlanFileReader, MowLawnUseCase mowLawnUseCase, MowerOutputWriter mowerOutputWriter) {
        this.mowingPlanFileReader = mowingPlanFileReader;
        this.mowLawnUseCase = mowLawnUseCase;
        this.mowerOutputWriter = mowerOutputWriter;
    }

    public static void main(String[] args) throws IOException {
        MowerCli cli = new MowerCli(new MowingPlanFileReader(), new MowLawnUseCase(), new MowerOutputWriter(System.out));
        System.exit(cli.run(args));
    }

    public int run(String[] args) throws IOException {
        try {
            return execute(args);
        } finally {
            mowerOutputWriter.flush();
        }
    }

    private int execute(String[] args) throws IOException {
        // pas de lecture sur l'entrée standard : un seul argument, le chemin du fichier
        if (args.length != 1) {
            mowerOutputWriter.writeError(USAGE);
            return EXIT_USAGE;
        }
        try {
            MowingPlan plan = mowingPlanFileReader.read(toPath(args[0]));
            mowerOutputWriter.write(mowLawnUseCase.mow(plan));
            return EXIT_SUCCESS;
        } catch (BusinessValidationException exception) {
            // le plan est validé en entier avant la simulation : aucune position n'a été écrite
            mowerOutputWriter.writeError(exception);
            return EXIT_ERROR;
        }
    }

    private static Path toPath(String argument) {
        try {
            return Path.of(argument);
        } catch (InvalidPathException exception) {
            throw new BusinessValidationException(BusinessErrorCode.FILE_NOT_FOUND, "file not found: " + argument);
        }
    }
}
