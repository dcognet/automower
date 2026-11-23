package com.automower.infrastructure.web.mowing.mapper;

import com.automower.domain.Lawn;
import com.automower.domain.Mower;
import com.automower.domain.MowerProgram;
import com.automower.domain.MowingPlan;
import com.automower.domain.Orientation;
import com.automower.domain.Position;
import com.automower.infrastructure.web.MissingMowerException;
import com.automower.infrastructure.web.mowing.model.MowingRequest;
import com.automower.infrastructure.web.mowing.model.MowingResponse;

import java.util.List;

public final class MowingWebMapper {

    private MowingWebMapper() {
        // vide
    }

    public static MowingPlan toMowingPlan(MowingRequest request) {
        Lawn lawn = new Lawn(request.lawn().maxX(), request.lawn().maxY());
        if (request.mowers().isEmpty()) {
            throw new MissingMowerException();
        }
        List<MowerProgram> programs = request.mowers().stream().map(MowingWebMapper::toProgram).toList();
        return new MowingPlan(lawn, programs);
    }

    public static MowingResponse toResponse(List<Mower> mowers) {
        return new MowingResponse(mowers.stream().map(MowingWebMapper::toResponse).toList());
    }

    private static MowerProgram toProgram(MowingRequest.Mower request) {
        Mower mower = new Mower(new Position(request.x(), request.y()), Orientation.fromCode(request.orientation()));
        return new MowerProgram(mower, request.instructions());
    }

    private static MowingResponse.Mower toResponse(Mower mower) {
        return new MowingResponse.Mower(mower.position().x(), mower.position().y(), String.valueOf(mower.orientation().code()));
    }
}
