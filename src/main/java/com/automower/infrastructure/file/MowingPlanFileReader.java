package com.automower.infrastructure.file;

import com.automower.domain.MowingPlan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MowingPlanFileReader {

    public MowingPlan read(Path path) throws IOException {
        byte[] content = Files.readAllBytes(path);
        return MowingPlanFileMapper.toMowingPlan(LineSplitter.split(new String(content, StandardCharsets.UTF_8)));
    }
}
