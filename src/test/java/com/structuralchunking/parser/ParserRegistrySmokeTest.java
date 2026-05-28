package com.structuralchunking.parser;

import com.structuralchunking.chunking.StructuralChunk;
import com.structuralchunking.chunking.StructuralChunker;
import com.structuralchunking.extractor.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserRegistrySmokeTest {
    @TempDir
    Path tempDir;

    @Test
    void smokeParsesWithNativeTikaUnstructuredAndDoclingParsers() throws Exception {
        Path file = tempDir.resolve("sample.csv");
        Files.writeString(file, "name,city\nAlice,Seattle\nBob,Portland\n", StandardCharsets.UTF_8);

        List<StructuralParser> parsers = ParserRegistry.defaultParsers(Map.of(
                "STRUCTURAL_UNSTRUCTURED_COMMAND", "cat",
                "STRUCTURAL_DOCLING_COMMAND", "cat"
        ));

        assertEquals(List.of("native", "apache-tika", "unstructured-io", "docling"), parserNames(parsers));
        for (StructuralParser parser : parsers) {
            assertTrue(parser.supports(FileType.CSV));

            StructuralDocument document = parser.parse(file);
            List<StructuralChunk> chunks = new StructuralChunker(100).chunk(document);

            assertEquals(parser.name(), document.parserName());
            assertFalse(document.fullText().isBlank(), parser.name());
            assertFalse(chunks.isEmpty(), parser.name());
        }
    }

    private static List<String> parserNames(List<StructuralParser> parsers) {
        return parsers.stream()
                .map(StructuralParser::name)
                .toList();
    }
}
