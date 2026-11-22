package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;

public record Lawn(int maxX, int maxY) {

    // circonférence de la Terre à l'équateur : 40 075 017 cases de 1 m par axe
    public static final int MAX_DIMENSION = 40_075_016;

    public Lawn {
        requireValidDimension(maxX);
        requireValidDimension(maxY);
        if (maxX == 0 && maxY == 0) {
            throw new BusinessValidationException(BusinessErrorCode.SINGLE_CELL_LAWN, "lawn 0 0 must contain more than one cell");
        }
    }

    public boolean contains(Position position) {
        return position.x() >= 0 && position.x() <= maxX && position.y() >= 0 && position.y() <= maxY;
    }

    private static void requireValidDimension(int dimension) {
        if (dimension < 0 || dimension > MAX_DIMENSION) {
            throw new BusinessValidationException(BusinessErrorCode.INVALID_LAWN_DIMENSION, "lawn dimension " + dimension + " must be between 0 and " + MAX_DIMENSION);
        }
    }
}
