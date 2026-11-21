package com.automower.domain;

import java.util.List;

public final class MowingPlanParser {

    private static final String SEPARATOR = " ";

    private MowingPlanParser() {
        // vide
    }

    public static MowingPlan parse(List<String> lines) {
        Lawn lawn = parseLawn(lines.get(0));
        Mower mower = parseMower(lines.get(1));
        return new MowingPlan(lawn, new MowerProgram(mower, lines.get(2)));
    }

    private static Lawn parseLawn(String line) {
        String[] values = line.split(SEPARATOR);
        return new Lawn(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
    }

    private static Mower parseMower(String line) {
        String[] values = line.split(SEPARATOR);
        Position position = new Position(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
        return new Mower(position, Orientation.fromCode(values[2].charAt(0)));
    }
}
