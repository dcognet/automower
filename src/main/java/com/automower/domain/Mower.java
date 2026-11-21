package com.automower.domain;

public record Mower(Position position, Orientation orientation) {

    public Mower execute(Instruction instruction, OccupancyGrid grid) {
        return switch (instruction) {
            case LEFT -> new Mower(position, orientation.left());
            case RIGHT -> new Mower(position, orientation.right());
            case FORWARD -> moveForward(grid);
        };
    }

    private Mower moveForward(OccupancyGrid grid) {
        Position target = position.next(orientation);
        // un F vers un bord ou une case occupée est ignoré, ce n'est pas une erreur
        return grid.isFree(target) ? new Mower(target, orientation) : this;
    }
}
