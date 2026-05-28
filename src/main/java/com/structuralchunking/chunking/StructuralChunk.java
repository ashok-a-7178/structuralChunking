package com.structuralchunking.chunking;

import java.nio.file.Path;
import java.util.Map;

public record StructuralChunk(
        String parserName,
        Path sourcePath,
        int index,
        String heading,
        String text,
        Map<String, String> metadata
) {
    public StructuralChunk {
        if (parserName == null || parserName.isBlank()) {
            throw new IllegalArgumentException("Parser name is required");
        }
        if (sourcePath == null) {
            throw new IllegalArgumentException("Source path is required");
        }
        if (index < 1) {
            throw new IllegalArgumentException("Chunk index must be positive");
        }
        heading = heading == null ? "" : heading.trim();
        text = text == null ? "" : text.trim();
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
