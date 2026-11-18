package com.jillliu.safeplate.eval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EvalDataLoader {
    private final ObjectMapper objectMapper;

    public EvalDataLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public List<EvalItem> load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<EvalItem> items = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            EvalItem item = objectMapper.readValue(lines.get(i), EvalItem.class);
            items.add(item);
        }
        return items;
    }

}
