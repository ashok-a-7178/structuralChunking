package com.structuralchunking.validation;

import java.nio.file.Path;
import java.util.List;

public record ParseValidationResult(
        String parserName,
        Path sourcePath,
        boolean parsed,
        boolean chunked,
        double score,
        List<String> issues
) {
    public ParseValidationResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}
