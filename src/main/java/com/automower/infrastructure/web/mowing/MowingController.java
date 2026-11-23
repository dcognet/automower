package com.automower.infrastructure.web.mowing;

import com.automower.application.MowLawnUseCase;
import com.automower.infrastructure.web.mowing.mapper.MowingWebMapper;
import com.automower.infrastructure.web.mowing.model.MowingRequest;
import com.automower.infrastructure.web.mowing.model.MowingResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mowings")
public class MowingController {

    private final MowLawnUseCase mowLawnUseCase;

    public MowingController(MowLawnUseCase mowLawnUseCase) {
        this.mowLawnUseCase = mowLawnUseCase;
    }

    @PostMapping
    public MowingResponse mow(@RequestBody MowingRequest request) {
        return MowingWebMapper.toResponse(mowLawnUseCase.mow(MowingWebMapper.toMowingPlan(request)));
    }
}
