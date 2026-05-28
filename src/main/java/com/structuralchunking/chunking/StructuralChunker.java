package com.structuralchunking.chunking;

import com.structuralchunking.parser.ElementType;
import com.structuralchunking.parser.StructuralDocument;
import com.structuralchunking.parser.StructuralElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StructuralChunker {
    private final int maxCharacters;

    public StructuralChunker(int maxCharacters) {
        if (maxCharacters < 100) {
            throw new IllegalArgumentException("maxCharacters must be at least 100");
        }
        this.maxCharacters = maxCharacters;
    }

    public List<StructuralChunk> chunk(StructuralDocument document) {
        List<StructuralChunk> chunks = new ArrayList<>();
        String currentHeading = "";
        StringBuilder buffer = new StringBuilder();

        for (StructuralElement element : document.elements()) {
            if (element.text().isBlank()) {
                continue;
            }
            if (!buffer.isEmpty() && buffer.length() + element.text().length() + 1 > maxCharacters) {
                chunks.add(newChunk(document, chunks.size() + 1, currentHeading, buffer.toString()));
                buffer.setLength(0);
            }
            if (element.type() == ElementType.HEADING || element.type() == ElementType.TITLE || element.type() == ElementType.SHEET) {
                currentHeading = element.text();
            }
            if (!buffer.isEmpty()) {
                buffer.append(System.lineSeparator());
            }
            buffer.append(element.text());
        }

        if (!buffer.isEmpty()) {
            chunks.add(newChunk(document, chunks.size() + 1, currentHeading, buffer.toString()));
        }
        return chunks;
    }

    private StructuralChunk newChunk(StructuralDocument document, int index, String heading, String text) {
        return new StructuralChunk(
                document.parserName(),
                document.sourcePath(),
                index,
                heading,
                text,
                Map.of("fileType", document.fileType().name())
        );
    }
}
