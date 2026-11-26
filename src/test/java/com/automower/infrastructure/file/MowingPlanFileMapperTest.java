package com.automower.infrastructure.file;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class MowingPlanFileMapperTest {

    @Test
    void shouldBuildLawnMowerAndInstructions_whenLinesAreConforming() {
        MowingPlan plan = MowingPlanFileMapper.toMowingPlan(List.of("10 12", "7 11 W", "LFRF"));

        MowerProgram expectedProgram = new MowerProgram(new Mower(new Position(7, 11), Orientation.WEST), "LFRF");
        assertThat(plan).isEqualTo(new MowingPlan(new Lawn(10, 12), List.of(expectedProgram)));
    }

    @Test
    void shouldBuildOneProgramPerPairOfLinesInFileOrder_whenFileDescribesSeveralMowers() {
        MowingPlan plan = MowingPlanFileMapper.toMowingPlan(List.of("5 5", "1 2 N", "LFLFLFLFF", "3 3 E", "FFRFFRFRRF", "0 0 S", "R"));

        assertThat(plan.programs()).containsExactly(
                new MowerProgram(new Mower(new Position(1, 2), Orientation.NORTH), "LFLFLFLFF"),
                new MowerProgram(new Mower(new Position(3, 3), Orientation.EAST), "FFRFFRFRRF"),
                new MowerProgram(new Mower(new Position(0, 0), Orientation.SOUTH), "R")
        );
    }

    @Test
    void shouldConvertCoordinates_whenAtMaximumLawnDimension() {
        MowingPlan plan = MowingPlanFileMapper.toMowingPlan(List.of("40075016 40075016", "40075016 0 N", "F"));

        assertThat(plan.lawn()).isEqualTo(new Lawn(40075016, 40075016));
        assertThat(plan.programs().getFirst().mower().position()).isEqualTo(new Position(40075016, 0));
    }

    @ParameterizedTest
    @MethodSource("inconsistentPlans")
    void shouldRejectLineInError_whenPlanIsWellFormedButInconsistent(List<String> lines, int expectedLine, Enum<?> expectedCode) {
        InvalidLineException exception = reject(lines);

        assertThat(codeOf(exception)).isEqualTo(expectedCode);
        assertThat(exception.getLineNumber()).isEqualTo(expectedLine);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LFA", "lf", "L F"})
    void shouldRejectInstructionLine_whenSequenceContainsUnknownInstruction(String instructions) {
        InvalidLineException exception = reject(List.of("5 5", "1 2 N", instructions));

        assertThat(codeOf(exception)).isEqualTo(BusinessErrorCode.INVALID_INSTRUCTION);
        assertThat(exception.getLineNumber()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"n", "X", "NE"})
    void shouldRejectPositionLine_whenOrientationIsUnknown(String orientation) {
        InvalidLineException exception = reject(List.of("5 5", "1 2 N", "F", "1 3 " + orientation, "F"));

        assertThat(codeOf(exception)).isEqualTo(BusinessErrorCode.INVALID_ORIENTATION);
        assertThat(exception).hasMessage("invalid orientation '" + orientation + "'");
        assertThat(exception.getLineNumber()).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(strings = {"05", "00", "+3", "-0", "-1", "\uFF15", "\u0665", "1.5", "1e3", "abc", "A", "1234567890", "99999999999999999999"})
    void shouldRejectCoordinate_whenNotWrittenAsAsciiNaturalNumber(String coordinate) {
        InvalidLineException onLawn = reject(List.of("5 " + coordinate, "1 2 N", "F"));
        InvalidLineException onPosition = reject(List.of("5 5", coordinate + " 2 N", "F"));

        assertThat(codeOf(onLawn)).isEqualTo(InputFileErrorCode.INVALID_COORDINATE);
        assertThat(onLawn).hasMessage("invalid coordinate '" + coordinate + "'");
        assertThat(onLawn.getLineNumber()).isEqualTo(1);
        assertThat(codeOf(onPosition)).isEqualTo(InputFileErrorCode.INVALID_COORDINATE);
        assertThat(onPosition.getLineNumber()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"5", "5 5 5", "5\t5", "5\u00A05", "5  5", " 5 5", "5 5 "})
    void shouldRejectLawnLine_whenNotExactlyTwoValuesSeparatedBySingleSpace(String lawnLine) {
        InvalidLineException exception = reject(List.of(lawnLine, "1 2 N", "F"));

        assertThat(codeOf(exception)).isEqualTo(InputFileErrorCode.INVALID_LAWN_LINE);
        assertThat(exception).hasMessage("lawn line must contain 2 values separated by a single space");
        assertThat(exception.getLineNumber()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 2", "1 2 N X", "1 2N", "1\t2 N", "1 2\u00A0N", "1  2 N", " 1 2 N", "1 2 N "})
    void shouldRejectPositionLine_whenNotExactlyThreeValuesSeparatedBySingleSpace(String positionLine) {
        InvalidLineException exception = reject(List.of("5 5", positionLine, "F"));

        assertThat(codeOf(exception)).isEqualTo(InputFileErrorCode.INVALID_POSITION_LINE);
        assertThat(exception).hasMessage("position line must contain 3 values separated by a single space");
        assertThat(exception.getLineNumber()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("filesWithEmptyLine")
    void shouldRejectEmptyLine_whenAnyLineIsEmpty(List<String> lines, int expectedLine) {
        InvalidLineException exception = reject(lines);

        assertThat(codeOf(exception)).isEqualTo(InputFileErrorCode.EMPTY_LINE);
        assertThat(exception).hasMessage("empty line");
        assertThat(exception.getLineNumber()).isEqualTo(expectedLine);
    }

    @ParameterizedTest
    @MethodSource("filesWithIsolatedCarriageReturn")
    void shouldRejectLineAsInvalidCharacter_whenLineContainsIsolatedCarriageReturn(List<String> lines, int expectedLine) {
        InvalidLineException exception = reject(lines);

        assertThat(codeOf(exception)).isEqualTo(InputFileErrorCode.INVALID_CHARACTER);
        assertThat(exception).hasMessage("invalid character '\r'");
        assertThat(exception.getLineNumber()).isEqualTo(expectedLine);
    }

    @Test
    void shouldRejectFirstLine_whenFileStartsWithByteOrderMark() {
        InvalidLineException exception = reject(List.of("\uFEFF5 5", "1 2 N", "F"));

        assertThat(codeOf(exception)).isEqualTo(InputFileErrorCode.UNEXPECTED_BYTE_ORDER_MARK);
        assertThat(exception).hasMessage("unexpected byte order mark");
        assertThat(exception.getLineNumber()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("filesWithMissingInstructionLine")
    void shouldRejectExpectedInstructionLine_whenPositionHasNoInstructionLine(List<String> lines, int expectedLine) {
        InvalidLineException exception = reject(lines);

        assertThat(codeOf(exception)).isEqualTo(InputFileErrorCode.MISSING_INSTRUCTIONS);
        assertThat(exception).hasMessage("missing instruction line");
        assertThat(exception.getLineNumber()).isEqualTo(expectedLine);
    }

    @Test
    void shouldRejectSecondLine_whenFileDefinesLawnButNoMower() {
        InvalidLineException exception = reject(List.of("5 5"));

        assertThat(codeOf(exception)).isEqualTo(InputFileErrorCode.MISSING_MOWER);
        assertThat(exception).hasMessage("missing mower position line");
        assertThat(exception.getLineNumber()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("filesWithSeveralErrors")
    void shouldReportOnlyFirstErrorInLineOrder_whenFileContainsSeveralErrors(List<String> lines, int expectedLine, Enum<?> expectedCode) {
        InvalidLineException exception = reject(lines);

        assertThat(codeOf(exception)).isEqualTo(expectedCode);
        assertThat(exception.getLineNumber()).isEqualTo(expectedLine);
    }

    private static Stream<Arguments> filesWithMissingInstructionLine() {
        return Stream.of(
                Arguments.of(List.of("5 5", "1 2 N"), 3),
                Arguments.of(List.of("5 5", "1 2 N", "F", "3 3 E"), 5)
        );
    }

    private static Stream<Arguments> inconsistentPlans() {
        return Stream.of(
                Arguments.of(List.of("0 0", "0 0 N", "F"), 1, BusinessErrorCode.SINGLE_CELL_LAWN),
                Arguments.of(List.of("40075017 5", "1 2 N", "F"), 1, BusinessErrorCode.INVALID_LAWN_DIMENSION),
                Arguments.of(List.of("999999999 5", "1 2 N", "F"), 1, BusinessErrorCode.INVALID_LAWN_DIMENSION),
                Arguments.of(List.of("5 5", "6 2 N", "F"), 2, BusinessErrorCode.START_OUTSIDE_LAWN),
                Arguments.of(List.of("5 5", "1 2 N", "F", "999999999 0 E", "F"), 4, BusinessErrorCode.START_OUTSIDE_LAWN),
                Arguments.of(List.of("5 5", "1 2 N", "F", "3 3 E", "L", "1 2 E", "R"), 6, BusinessErrorCode.DUPLICATE_START)
        );
    }

    private static Stream<Arguments> filesWithEmptyLine() {
        return Stream.of(
                Arguments.of(List.of("", "1 2 N", "F"), 1),
                Arguments.of(List.of("5 5", "", "1 2 N", "F"), 2),
                // une ligne d'instructions vide est d'abord une ligne vide (RM-21) avant d'être une séquence vide (RM-20)
                Arguments.of(List.of("5 5", "1 2 N", ""), 3),
                Arguments.of(List.of("5 5", "1 2 N", "F", ""), 4)
        );
    }

    private static Stream<Arguments> filesWithIsolatedCarriageReturn() {
        return Stream.of(
                Arguments.of(List.of("5\r5", "1 2 N", "F"), 1),
                Arguments.of(List.of("5 5", "1 2 N\r", "F"), 2),
                Arguments.of(List.of("5 5", "1 2 N", "F\rL"), 3)
        );
    }

    private static Stream<Arguments> filesWithSeveralErrors() {
        return Stream.of(
                Arguments.of(List.of("5 A", "1 2 X", ""), 1, InputFileErrorCode.INVALID_COORDINATE),
                Arguments.of(List.of("5 5", "1 2 X", "LFA"), 2, BusinessErrorCode.INVALID_ORIENTATION),
                Arguments.of(List.of("5 5", "1 2 N", "LFA", "-1 3 E"), 3, BusinessErrorCode.INVALID_INSTRUCTION),
                Arguments.of(List.of("5 5", "1 2 N", "F", "3 3 n", "X"), 4, BusinessErrorCode.INVALID_ORIENTATION),
                Arguments.of(List.of("5 5", "1 2 N", "", "3 3 n", "X"), 3, InputFileErrorCode.EMPTY_LINE),
                Arguments.of(List.of("5 5", "1 2 N", "F\r", "", "X"), 3, InputFileErrorCode.INVALID_CHARACTER),
                Arguments.of(List.of("0 0", "1 2 X", "LFA"), 1, BusinessErrorCode.SINGLE_CELL_LAWN),
                Arguments.of(List.of("5 5", "6 2 N", "LFA"), 2, BusinessErrorCode.START_OUTSIDE_LAWN),
                Arguments.of(List.of("5 5", "1 2 N", "LFA", "1 2 E", "F"), 3, BusinessErrorCode.INVALID_INSTRUCTION),
                Arguments.of(List.of("5 5", "1 2 N", "F", "1 2 E", "LFA"), 4, BusinessErrorCode.DUPLICATE_START),
                Arguments.of(List.of("5 5", "1 2 N", "F", "1 2 X", "F"), 4, BusinessErrorCode.INVALID_ORIENTATION)
        );
    }

    private static InvalidLineException reject(List<String> lines) {
        return catchThrowableOfType(InvalidLineException.class, () -> MowingPlanFileMapper.toMowingPlan(lines));
    }

    private static Enum<?> codeOf(InvalidLineException exception) {
        return switch (exception.getCause()) {
            case InputFileException cause -> cause.getCode();
            case BusinessValidationException cause -> cause.getCode();
            default -> throw new AssertionError("unexpected cause " + exception.getCause());
        };
    }
}
