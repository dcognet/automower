package com.automower.infrastructure.file;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
import com.automower.domain.StartingPositions;
import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MowingPlanFileMapper {

    // valeurs séparées par exactement une espace U+0020, sans espace en début ni en fin de ligne
    private static final Pattern LAWN_LINE = Pattern.compile("([^ ]+) ([^ ]+)");
    private static final Pattern POSITION_LINE = Pattern.compile("([^ ]+) ([^ ]+) ([^ ]+)");
    // au plus 9 chiffres ASCII : la conversion en int ne peut pas déborder
    private static final Pattern COORDINATE = Pattern.compile("0|[1-9][0-9]{0,8}");
    private static final char BYTE_ORDER_MARK = '\uFEFF';
    private static final char CARRIAGE_RETURN = '\r';
    private static final int LAWN_LINE_INDEX = 0;
    private static final int FIRST_MOWER_LINE_INDEX = 1;
    private static final int LINES_PER_MOWER = 2;

    private MowingPlanFileMapper() {
        // vide
    }

    public static MowingPlan toMowingPlan(List<String> lines) {
        Lawn lawn = mapLine(lines, LAWN_LINE_INDEX, MowingPlanFileMapper::toLawn);
        requireLine(lines, FIRST_MOWER_LINE_INDEX, BusinessErrorCode.MISSING_MOWER, "missing mower position line");
        StartingPositions startingPositions = new StartingPositions(lawn);
        List<MowerProgram> programs = new ArrayList<>();
        // chaque tondeuse est un couple de lignes consécutives : position, puis instructions
        // les départs sont aussi validés par MowingPlan, mais ici au fil des lignes : la première erreur du fichier doit primer (RM-23)
        for (int i = FIRST_MOWER_LINE_INDEX; i < lines.size(); i += LINES_PER_MOWER) {
            Mower mower = mapLine(lines, i, line -> toStartingMower(line, startingPositions));
            requireLine(lines, i + 1, BusinessErrorCode.MISSING_INSTRUCTIONS, "missing instruction line");
            programs.add(mapLine(lines, i + 1, instructions -> new MowerProgram(mower, instructions)));
        }
        return new MowingPlan(lawn, programs);
    }

    private static void requireLine(List<String> lines, int index, BusinessErrorCode code, String detail) {
        // une ligne absente est signalée sur la ligne attendue
        if (index >= lines.size()) {
            throw new BusinessValidationException(code, detail).atLine(lineNumber(index));
        }
    }

    private static <T> T mapLine(List<String> lines, int index, Function<String, T> mapper) {
        try {
            String line = lines.get(index);
            checkLineStructure(line, index);
            return mapper.apply(line);
        } catch (BusinessValidationException exception) {
            throw exception.atLine(lineNumber(index));
        }
    }

    private static void checkLineStructure(String line, int index) {
        // le BOM est décodé en U+FEFF : il ne peut apparaître qu'en tête de la première ligne
        if (index == LAWN_LINE_INDEX && !line.isEmpty() && line.charAt(0) == BYTE_ORDER_MARK) {
            throw new BusinessValidationException(BusinessErrorCode.UNEXPECTED_BYTE_ORDER_MARK, "unexpected byte order mark");
        }
        if (line.isEmpty()) {
            throw new BusinessValidationException(BusinessErrorCode.EMPTY_LINE, "empty line");
        }
        // un \r suivi de \n a été retiré au découpage : celui qui reste est une fin de ligne invalide
        if (line.indexOf(CARRIAGE_RETURN) >= 0) {
            throw new BusinessValidationException(BusinessErrorCode.INVALID_CHARACTER, "invalid character '" + CARRIAGE_RETURN + "'");
        }
    }

    private static int lineNumber(int index) {
        return index + 1;
    }

    private static Lawn toLawn(String line) {
        Matcher matcher = LAWN_LINE.matcher(line);
        if (!matcher.matches()) {
            throw new BusinessValidationException(BusinessErrorCode.INVALID_LAWN_LINE, "lawn line must contain 2 values separated by a single space");
        }
        return new Lawn(toCoordinate(matcher.group(1)), toCoordinate(matcher.group(2)));
    }

    private static Mower toStartingMower(String line, StartingPositions startingPositions) {
        Mower mower = toMower(line);
        startingPositions.add(mower.position());
        return mower;
    }

    private static Mower toMower(String line) {
        Matcher matcher = POSITION_LINE.matcher(line);
        if (!matcher.matches()) {
            throw new BusinessValidationException(BusinessErrorCode.INVALID_POSITION_LINE, "position line must contain 3 values separated by a single space");
        }
        Position position = new Position(toCoordinate(matcher.group(1)), toCoordinate(matcher.group(2)));
        return new Mower(position, Orientation.fromCode(matcher.group(3)));
    }

    private static int toCoordinate(String value) {
        if (!COORDINATE.matcher(value).matches()) {
            throw new BusinessValidationException(BusinessErrorCode.INVALID_COORDINATE, "invalid coordinate '" + value + "'");
        }
        return Integer.parseInt(value);
    }
}
