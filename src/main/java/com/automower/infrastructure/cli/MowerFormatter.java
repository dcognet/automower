package com.automower.infrastructure.cli;

import com.automower.domain.Mower;

public final class MowerFormatter {

    private static final String SEPARATOR = " ";

    private MowerFormatter() {
        // vide
    }

    public static String format(Mower mower) {
        return mower.position().x() + SEPARATOR + mower.position().y() + SEPARATOR + mower.orientation().code();
    }
}
