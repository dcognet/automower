package com.automower.infrastructure.web.mowing.model;

import java.util.List;

public record MowingResponse(List<Mower> items) {

    public record Mower(int x, int y, String orientation) {
    }
}
