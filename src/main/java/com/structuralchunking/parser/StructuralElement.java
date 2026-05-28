package com.structuralchunking.parser;

import java.util.Map;

public record StructuralElement(ElementType type, String text, int level, Map<String, String> metadata) {
    public StructuralElement {
        if (type == null) {
            throw new IllegalArgumentException("Element type is required");
        }
        text = text == null ? "" : text.trim();
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static StructuralElement of(ElementType type, String text) {
        return new StructuralElement(type, text, 0, Map.of());
    }

    public static StructuralElement of(ElementType type, String text, int level) {
        return new StructuralElement(type, text, level, Map.of());
    }
}
