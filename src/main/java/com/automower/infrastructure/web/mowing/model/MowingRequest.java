package com.automower.infrastructure.web.mowing.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;

public record MowingRequest(Lawn lawn, @JsonSetter(contentNulls = Nulls.FAIL) List<Mower> mowers) {

    public record Lawn(int maxX, int maxY) {
    }

    public record Mower(int x, int y, String orientation, String instructions) {
    }
}
