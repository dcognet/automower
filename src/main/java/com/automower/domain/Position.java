package com.automower.domain;

public record Position(int x, int y) {

    public Position next(Orientation orientation) {
        return new Position(x + orientation.dx(), y + orientation.dy());
    }
}
