package com.automower.domain;

public record MowingPlan(Lawn lawn, MowerProgram program) {

    public Mower run() {
        return program.runOn(lawn);
    }
}
