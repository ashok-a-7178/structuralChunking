package com.structuralchunking.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ParserRegistry {
    private ParserRegistry() {
    }

    public static List<StructuralParser> defaultParsers() {
        return defaultParsers(System.getenv());
    }

    static List<StructuralParser> defaultParsers(Map<String, String> environment) {
        List<StructuralParser> parsers = new ArrayList<>();
        parsers.add(new NativeStructuralParser());
        parsers.add(new TikaStructuralParser());
        addExternalParser(parsers, "unstructured-io", "STRUCTURAL_UNSTRUCTURED_COMMAND", environment);
        addExternalParser(parsers, "docling", "STRUCTURAL_DOCLING_COMMAND", environment);
        return List.copyOf(parsers);
    }

    private static void addExternalParser(
            List<StructuralParser> parsers,
            String name,
            String environmentVariable,
            Map<String, String> environment
    ) {
        String command = environment.get(environmentVariable);
        if (command != null && !command.isBlank()) {
            parsers.add(new ExternalCommandStructuralParser(name, Arrays.asList(command.trim().split("\\s+"))));
        }
    }
}
