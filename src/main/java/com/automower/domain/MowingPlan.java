package com.automower.domain;

import java.util.List;

public record MowingPlan(Lawn lawn, List<MowerProgram> programs) {

    public MowingPlan {
        programs = List.copyOf(programs);
    }

    public List<Mower> run() {
        // exécution séquentielle : la tondeuse n+1 ne bouge qu'une fois la tondeuse n arrêtée
        return programs.stream().map(program -> program.runOn(lawn)).toList();
    }
}
