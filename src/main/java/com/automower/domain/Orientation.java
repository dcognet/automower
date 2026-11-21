package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;

public enum Orientation {
    NORTH('N', 0, 1),
    EAST('E', 1, 0),
    SOUTH('S', 0, -1),
    WEST('W', -1, 0);

    private final char code;
    private final int dx;
    private final int dy;

    Orientation(char code, int dx, int dy) {
        this.code = code;
        this.dx = dx;
        this.dy = dy;
    }

    public static Orientation fromCode(char code) {
        return switch (code) {
            case 'N' -> NORTH;
            case 'E' -> EAST;
            case 'S' -> SOUTH;
            case 'W' -> WEST;
            default -> throw new BusinessValidationException(BusinessErrorCode.INVALID_ORIENTATION, "invalid orientation '" + code + "'");
        };
    }

    public char code() {
        return code;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public Orientation left() {
        return values()[(ordinal() + 3) % 4];
    }

    public Orientation right() {
        return values()[(ordinal() + 1) % 4];
    }
}
