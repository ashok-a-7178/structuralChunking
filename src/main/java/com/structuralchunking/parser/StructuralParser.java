package com.structuralchunking.parser;

import com.structuralchunking.extractor.FileType;

import java.io.IOException;
import java.nio.file.Path;

public interface StructuralParser {
    String name();

    boolean supports(FileType fileType);

    StructuralDocument parse(Path filePath) throws IOException;
}
