package com.structuralchunking.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ParserRegistry {
    private ParserRegistry() {
    }

    public static List<StructuralParser> defaultParsers() {
        List<StructuralParser> parsers = new ArrayList<>();
        parsers.add(new NativeStructuralParser());
        parsers.add(new TikaStructuralParser());
        addExternalParser(parsers, "unstructured-io", "STRUCTURAL_UNSTRUCTURED_COMMAND");
        addExternalParser(parsers, "docling", "STRUCTURAL_DOCLING_COMMAND");
        return List.copyOf(parsers);
    }

    private static void addExternalParser(List<StructuralParser> parsers, String name, String environmentVariable) {
        String command = System.getenv(environmentVariable);
        if (command != null && !command.isBlank()) {
            parsers.add(new ExternalCommandStructuralParser(name, Arrays.asList(command.trim().split("\\s+"))));
        }
    }
}
