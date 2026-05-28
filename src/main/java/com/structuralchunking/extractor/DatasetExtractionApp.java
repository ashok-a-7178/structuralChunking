package com.structuralchunking.extractor;

import com.structuralchunking.chunking.StructuralChunk;
import com.structuralchunking.chunking.StructuralChunker;
import com.structuralchunking.parser.ParserRegistry;
import com.structuralchunking.parser.StructuralDocument;
import com.structuralchunking.parser.StructuralParser;
import com.structuralchunking.validation.MeaningfulChunkValidator;
import com.structuralchunking.validation.ParseValidationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class DatasetExtractionApp {
    private static final int MAX_CHUNK_CHARACTERS = 1_500;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Pass a dataset folder path as the first argument");
        }

        List<StructuralParser> parsers = ParserRegistry.defaultParsers();
        StructuralChunker chunker = new StructuralChunker(MAX_CHUNK_CHARACTERS);
        MeaningfulChunkValidator validator = new MeaningfulChunkValidator(MAX_CHUNK_CHARACTERS);
        try (Stream<Path> paths = Files.walk(Path.of(args[0]))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                FileType fileType;
                try {
                    fileType = FileTypeDetector.detect(path);
                } catch (Exception ex) {
                    System.out.println("Skipping " + path.getFileName() + ": " + ex.getMessage());
                    return;
                }
                for (StructuralParser parser : parsers) {
                    if (!parser.supports(fileType)) {
                        continue;
                    }
                    try {
                        StructuralDocument document = parser.parse(path);
                        List<StructuralChunk> chunks = chunker.chunk(document);
                        ParseValidationResult result = validator.validate(document, chunks);
                        System.out.printf(
                                "%s | %s | elements=%d chunks=%d score=%.2f issues=%s%n",
                                path.getFileName(),
                                parser.name(),
                                document.elements().size(),
                                chunks.size(),
                                result.score(),
                                result.issues()
                        );
                    } catch (Exception ex) {
                        System.out.println(path.getFileName() + " | " + parser.name() + " failed: " + ex.getMessage());
                    }
                }
            });
        }
    }
}
