package com.automower.infrastructure.file;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;

import java.util.List;

public final class MowingPlanFileMapper {

    private static final String SEPARATOR = " ";

    private MowingPlanFileMapper() {
        // vide
    }

    public static MowingPlan toMowingPlan(List<String> lines) {
        Lawn lawn = toLawn(lines.get(0));
        Mower mower = toMower(lines.get(1));
        return new MowingPlan(lawn, new MowerProgram(mower, lines.get(2)));
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
