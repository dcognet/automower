package com.automower.domain;

public record Mower(Position position, Orientation orientation) {

    public Mower execute(Instruction instruction, Lawn lawn) {
        return switch (instruction) {
            case LEFT -> new Mower(position, orientation.left());
            case RIGHT -> new Mower(position, orientation.right());
            case FORWARD -> moveForward(lawn);
        };
    }

    private Mower moveForward(Lawn lawn) {
        Position target = position.next(orientation);
        // un F qui ferait sortir de la pelouse est ignoré, ce n'est pas une erreur
        return lawn.contains(target) ? new Mower(target, orientation) : this;
    }
}
