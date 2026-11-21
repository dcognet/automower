package com.automower.infrastructure.file;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;

import java.util.ArrayList;
import java.util.List;

public final class MowingPlanFileMapper {

    private static final String SEPARATOR = " ";
    private static final int FIRST_MOWER_LINE = 1;
    private static final int LINES_PER_MOWER = 2;

    private MowingPlanFileMapper() {
        // vide
    }

    public static MowingPlan toMowingPlan(List<String> lines) {
        Lawn lawn = toLawn(lines.get(0));
        List<MowerProgram> programs = new ArrayList<>();
        // chaque tondeuse est un couple de lignes consécutives : position, puis instructions
        for (int i = FIRST_MOWER_LINE; i < lines.size(); i += LINES_PER_MOWER) {
            programs.add(new MowerProgram(toMower(lines.get(i)), lines.get(i + 1)));
        }
        return new MowingPlan(lawn, programs);
    }

    private static Lawn toLawn(String line) {
        String[] values = line.split(SEPARATOR);
        return new Lawn(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
    }

    private static Mower toMower(String line) {
        String[] values = line.split(SEPARATOR);
        Position position = new Position(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
        return new Mower(position, Orientation.fromCode(values[2].charAt(0)));
    }
}
