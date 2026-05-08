package com.structuralchunking.extractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DatasetExtractionApp {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Pass a dataset folder path as the first argument");
        }

        RawFileTextExtractor extractor = new RawFileTextExtractor();
        try (Stream<Path> paths = Files.walk(Path.of(args[0]))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    String text = extractor.extractText(path);
                    System.out.println("=== " + path.getFileName() + " ===");
                    System.out.println(text);
                } catch (Exception ex) {
                    System.out.println("Skipping " + path.getFileName() + ": " + ex.getMessage());
                }
            });
        }
    }
}
