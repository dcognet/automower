package com.automower.domain;

public record Position(int x, int y) {

    // constante de Fibonacci (2^32 / nombre d'or), impaire : répartit x sur tous les bits du hash
    private static final int HASH_MULTIPLIER = 0x9E3779B9;

    public Position next(Orientation orientation) {
        return new Position(x + orientation.dx(), y + orientation.dy());
    }

    // le hash par défaut d'un record (31 * x + y) fait collisionner les cases d'une grille dense de centaines de milliers de tondeuses
    @Override
    public int hashCode() {
        return x * HASH_MULTIPLIER ^ y;
    }
}
