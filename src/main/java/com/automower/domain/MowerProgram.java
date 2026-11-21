package com.automower.domain;

public record MowerProgram(Mower mower, String instructions) {

    public Mower runOn(Lawn lawn) {
        Mower current = mower;
        for (int i = 0; i < instructions.length(); i++) {
            current = current.execute(Instruction.fromCode(instructions.charAt(i)), lawn);
        }
        return current;
    }
}
