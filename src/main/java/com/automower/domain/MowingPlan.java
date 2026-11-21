package com.automower.domain;

import java.util.ArrayList;
import java.util.List;

public record MowingPlan(Lawn lawn, List<MowerProgram> programs) {

    public MowingPlan {
        programs = List.copyOf(programs);
    }

    public List<Mower> run() {
        // toutes les tondeuses sont déployées dès le départ : chaque case de départ est occupée
        List<Position> startPositions = programs.stream().map(program -> program.mower().position()).toList();
        OccupancyGrid grid = new OccupancyGrid(lawn, startPositions);
        List<Mower> finalMowers = new ArrayList<>();
        // exécution séquentielle : la tondeuse n+1 ne bouge qu'une fois la tondeuse n arrêtée
        for (MowerProgram program : programs) {
            grid.leave(program.mower().position());
            Mower finalMower = program.runOn(grid);
            grid.occupy(finalMower.position());
            finalMowers.add(finalMower);
        }
        return finalMowers;
    }
}
