package com.automower.application;

import com.automower.domain.Mower;
import com.automower.domain.MowingPlan;
import com.automower.domain.MowingPlanParser;

import java.util.List;

public class MowLawnUseCase {

    public Mower mow(List<String> lines) {
        MowingPlan plan = MowingPlanParser.parse(lines);
        return plan.run();
    }
}
