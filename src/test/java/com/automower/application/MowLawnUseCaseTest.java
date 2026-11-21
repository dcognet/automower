package com.automower.application;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
import com.automower.infrastructure.cli.MowerOutputWriter;
import com.automower.infrastructure.file.MowingPlanFileReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MowLawnUseCaseTest {

    private static final Path INPUT_FILE = Path.of("input.txt");

    @Mock
    private MowingPlanFileReader mowingPlanFileReader;

    @Mock
    private MowerOutputWriter mowerOutputWriter;

    @InjectMocks
    private MowLawnUseCase mowLawnUseCase;

    @Test
    void shouldWriteFinalMower_whenPlanDescribesOneMower() throws IOException {
        MowerProgram program = new MowerProgram(new Mower(new Position(1, 2), Orientation.NORTH), "LFLFLFLFF");
        when(mowingPlanFileReader.read(INPUT_FILE)).thenReturn(new MowingPlan(new Lawn(5, 5), program));

        mowLawnUseCase.mow(INPUT_FILE);

        verify(mowerOutputWriter).write(new Mower(new Position(1, 3), Orientation.NORTH));
    }
}
