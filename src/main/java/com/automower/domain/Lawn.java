package com.automower.domain;

public record Lawn(int maxX, int maxY) {

    public boolean contains(Position position) {
        return position.x() >= 0 && position.x() <= maxX && position.y() >= 0 && position.y() <= maxY;
    }
}
