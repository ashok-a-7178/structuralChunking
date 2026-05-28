package com.structuralchunking.validation;

import com.structuralchunking.chunking.StructuralChunk;
import com.structuralchunking.parser.ElementType;
import com.structuralchunking.parser.StructuralDocument;

import java.util.ArrayList;
import java.util.List;

public class MeaningfulChunkValidator {
    private final int maxChunkCharacters;

    public MeaningfulChunkValidator(int maxChunkCharacters) {
        this.maxChunkCharacters = maxChunkCharacters;
    }

    public ParseValidationResult validate(StructuralDocument document, List<StructuralChunk> chunks) {
        List<String> issues = new ArrayList<>();
        if (document.elements().isEmpty() || document.fullText().isBlank()) {
            issues.add("Parser produced no text");
        }
        if (document.elements().stream().allMatch(element -> element.type() == ElementType.TEXT)) {
            issues.add("Parser did not expose structural elements");
        }
        if (chunks.isEmpty()) {
            issues.add("Chunker produced no chunks");
        }
        for (StructuralChunk chunk : chunks) {
            if (chunk.text().isBlank()) {
                issues.add("Chunk " + chunk.index() + " is blank");
            }
            if (chunk.text().length() > maxChunkCharacters) {
                issues.add("Chunk " + chunk.index() + " exceeds " + maxChunkCharacters + " characters");
            }
        }
        double score = Math.max(0.0, 1.0 - (issues.size() * 0.25));
        return new ParseValidationResult(
                document.parserName(),
                document.sourcePath(),
                !document.elements().isEmpty(),
                !chunks.isEmpty(),
                score,
                issues
        );
    }
}
