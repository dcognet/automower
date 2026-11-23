package com.automower.application;

import com.automower.domain.Mower;
import com.automower.domain.MowingPlan;

import java.util.List;

public class MowLawnUseCase {

    public List<Mower> mow(MowingPlan plan) {
        return plan.run();
    }
}
