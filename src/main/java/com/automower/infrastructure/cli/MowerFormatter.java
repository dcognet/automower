package com.automower.infrastructure.cli;

import com.automower.domain.Mower;

public final class MowerFormatter {

    private MowerFormatter() {
        // vide
    }

    public static String format(Mower mower) {
        return mower.position().x() + " " + mower.position().y() + " " + mower.orientation().code();
    }
}
