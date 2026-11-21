package com.automower.infrastructure.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class InputFileReader {

    public List<String> readLines(Path path) throws IOException {
        byte[] content = Files.readAllBytes(path);
        return LineSplitter.split(new String(content, StandardCharsets.UTF_8));
    }
}
