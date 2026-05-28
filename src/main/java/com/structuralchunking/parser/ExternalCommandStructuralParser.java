package com.structuralchunking.parser;

import com.structuralchunking.extractor.FileType;
import com.structuralchunking.extractor.FileTypeDetector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ExternalCommandStructuralParser implements StructuralParser {
    private final String name;
    private final List<String> command;

    public ExternalCommandStructuralParser(String name, List<String> command) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Parser name is required");
        }
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("Command is required");
        }
        this.name = name;
        this.command = List.copyOf(command);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean supports(FileType fileType) {
        return fileType != FileType.UNKNOWN;
    }

    @Override
    public StructuralDocument parse(Path filePath) throws IOException {
        FileType fileType = FileTypeDetector.detect(filePath);
        if (!supports(fileType)) {
            throw new IllegalArgumentException("Unsupported file type for file: " + filePath);
        }

        ProcessBuilder builder = new ProcessBuilder(buildCommand(filePath));
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException(name + " parser exited with code " + exitCode + ": " + output);
            }
            return new StructuralDocument(
                    name,
                    filePath,
                    fileType,
                    output.isBlank() ? List.of() : List.of(StructuralElement.of(ElementType.TEXT, output)),
                    Map.of("command", String.join(" ", command))
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(name + " parser was interrupted", ex);
        }
    }

    private List<String> buildCommand(Path filePath) {
        java.util.ArrayList<String> commandWithFile = new java.util.ArrayList<>(command);
        commandWithFile.add(filePath.toString());
        return commandWithFile;
    }
}
