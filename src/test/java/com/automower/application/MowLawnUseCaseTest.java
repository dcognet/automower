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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.inOrder;
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
    void shouldWriteFinalMowersInFileOrder_whenPlanDescribesSeveralMowers() throws IOException {
        MowerProgram first = new MowerProgram(new Mower(new Position(1, 2), Orientation.NORTH), "LFLFLFLFF");
        MowerProgram second = new MowerProgram(new Mower(new Position(3, 3), Orientation.EAST), "FFRFFRFRRF");
        when(mowingPlanFileReader.read(INPUT_FILE)).thenReturn(new MowingPlan(new Lawn(5, 5), List.of(first, second)));

        mowLawnUseCase.mow(INPUT_FILE);

        InOrder inOrder = inOrder(mowerOutputWriter);
        inOrder.verify(mowerOutputWriter).write(new Mower(new Position(1, 3), Orientation.NORTH));
        inOrder.verify(mowerOutputWriter).write(new Mower(new Position(5, 1), Orientation.EAST));
        inOrder.verifyNoMoreInteractions();
    }
}
