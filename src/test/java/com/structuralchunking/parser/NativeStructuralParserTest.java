package com.structuralchunking.parser;

import com.structuralchunking.chunking.StructuralChunk;
import com.structuralchunking.chunking.StructuralChunker;
import com.structuralchunking.validation.MeaningfulChunkValidator;
import com.structuralchunking.validation.ParseValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeStructuralParserTest {
    @TempDir
    Path tempDir;

    @Test
    void parsesHtmlIntoStructuralElements() throws Exception {
        Path file = tempDir.resolve("sample.html");
        Files.writeString(
                file,
                "<html><head><title>Doc title</title></head><body><h1>Overview</h1><p>First paragraph.</p><ul><li>Item one</li></ul></body></html>",
                StandardCharsets.UTF_8
        );

        StructuralDocument document = new NativeStructuralParser().parse(file);

        assertEquals("native", document.parserName());
        assertTrue(document.elements().stream().anyMatch(element -> element.type() == ElementType.TITLE && element.text().equals("Doc title")));
        assertTrue(document.elements().stream().anyMatch(element -> element.type() == ElementType.HEADING && element.level() == 1));
        assertTrue(document.elements().stream().anyMatch(element -> element.type() == ElementType.PARAGRAPH));
        assertTrue(document.elements().stream().anyMatch(element -> element.type() == ElementType.LIST_ITEM));
    }

    @Test
    void chunksAndValidatesParsedContent() throws Exception {
        Path file = tempDir.resolve("sample.csv");
        Files.writeString(file, "name,city\nAlice,Seattle\nBob,Portland\n", StandardCharsets.UTF_8);
        StructuralDocument document = new NativeStructuralParser().parse(file);

        List<StructuralChunk> chunks = new StructuralChunker(100).chunk(document);
        ParseValidationResult result = new MeaningfulChunkValidator(100).validate(document, chunks);

        assertFalse(chunks.isEmpty());
        assertTrue(result.parsed());
        assertTrue(result.chunked());
        assertTrue(result.issues().isEmpty(), () -> String.join(", ", result.issues()));
    }
}
