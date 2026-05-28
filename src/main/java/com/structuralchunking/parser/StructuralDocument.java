package com.structuralchunking.parser;

import com.structuralchunking.extractor.FileType;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record StructuralDocument(
        String parserName,
        Path sourcePath,
        FileType fileType,
        List<StructuralElement> elements,
        Map<String, String> metadata
) {
    public StructuralDocument {
        if (parserName == null || parserName.isBlank()) {
            throw new IllegalArgumentException("Parser name is required");
        }
        if (sourcePath == null) {
            throw new IllegalArgumentException("Source path is required");
        }
        if (fileType == null) {
            throw new IllegalArgumentException("File type is required");
        }
        elements = elements == null ? List.of() : List.copyOf(elements);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public String fullText() {
        return elements.stream()
                .map(StructuralElement::text)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
