package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;

import java.util.HashSet;
import java.util.Set;

public final class StartingPositions {

    private final Lawn lawn;
    private final Set<Position> taken = new HashSet<>();

    public StartingPositions(Lawn lawn) {
        this.lawn = lawn;
    }

    public void add(Position position) {
        if (!lawn.contains(position)) {
            throw new BusinessValidationException(BusinessErrorCode.START_OUTSIDE_LAWN, "starting position " + describe(position) + " is outside the lawn");
        }
        // deux tondeuses ne démarrent jamais sur la même case, quelle que soit leur orientation
        if (!taken.add(position)) {
            throw new BusinessValidationException(BusinessErrorCode.DUPLICATE_START, "starting position " + describe(position) + " is already taken");
        }
    }

    private static String describe(Position position) {
        return position.x() + " " + position.y();
    }
}
