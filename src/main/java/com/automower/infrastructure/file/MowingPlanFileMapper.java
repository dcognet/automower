package com.automower.infrastructure.file;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
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
    private static final int LAWN_LINE_INDEX = 0;
    private static final int FIRST_MOWER_LINE_INDEX = 1;
    private static final int LINES_PER_MOWER = 2;

    private MowingPlanFileMapper() {
        // vide
    }

    public static MowingPlan toMowingPlan(List<String> lines) {
        Lawn lawn = mapLine(lines, LAWN_LINE_INDEX, MowingPlanFileMapper::toLawn);
        if (lines.size() <= FIRST_MOWER_LINE_INDEX) {
            throw new BusinessValidationException(BusinessErrorCode.MISSING_MOWER, "missing mower position line").atLine(lineNumber(FIRST_MOWER_LINE_INDEX));
        }
        List<MowerProgram> programs = new ArrayList<>();
        // chaque tondeuse est un couple de lignes consécutives : position, puis instructions
        for (int i = FIRST_MOWER_LINE_INDEX; i < lines.size(); i += LINES_PER_MOWER) {
            Mower mower = mapLine(lines, i, MowingPlanFileMapper::toMower);
            if (i + 1 >= lines.size()) {
                throw new BusinessValidationException(BusinessErrorCode.MISSING_INSTRUCTIONS, "missing instruction line").atLine(lineNumber(i + 1));
            }
            programs.add(mapLine(lines, i + 1, instructions -> new MowerProgram(mower, instructions)));
        }
        return new MowingPlan(lawn, programs);
    }

    private static <T> T mapLine(List<String> lines, int index, Function<String, T> mapper) {
        try {
            return mapper.apply(lines.get(index));
        } catch (BusinessValidationException exception) {
            throw exception.atLine(lineNumber(index));
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
