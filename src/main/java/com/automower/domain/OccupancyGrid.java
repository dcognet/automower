package com.automower.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class OccupancyGrid {

    private final Lawn lawn;
    // grille creuse : seules les cases occupées sont stockées, la pelouse n'est jamais matérialisée
    private final Set<Position> occupiedCells;

    public OccupancyGrid(Lawn lawn, Collection<Position> occupiedCells) {
        this.lawn = lawn;
        this.occupiedCells = new HashSet<>(occupiedCells);
    }

    public boolean isFree(Position position) {
        return lawn.contains(position) && !occupiedCells.contains(position);
    }

    public void leave(Position position) {
        occupiedCells.remove(position);
    }

    public void occupy(Position position) {
        occupiedCells.add(position);
    }
}
