package com.automower.domain;

public record MowerProgram(Mower mower, String instructions) {

    public MowerProgram {
        // séquence validée à la construction : la simulation ne peut plus échouer
        for (int i = 0; i < instructions.length(); i++) {
            Instruction.fromCode(instructions.charAt(i));
        }
    }

    public Mower runOn(OccupancyGrid grid) {
        Mower current = mower;
        for (int i = 0; i < instructions.length(); i++) {
            current = current.execute(Instruction.fromCode(instructions.charAt(i)), grid);
        }
        return current;
    }
}
