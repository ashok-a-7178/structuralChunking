package com.structuralchunking.parser;

import com.structuralchunking.extractor.FileType;
import com.structuralchunking.extractor.FileTypeDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class TikaStructuralParser implements StructuralParser {
    @Override
    public String name() {
        return "apache-tika";
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

        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler(-1);
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            new AutoDetectParser().parse(inputStream, handler, metadata, new ParseContext());
        } catch (SAXException | TikaException ex) {
            throw new IOException("Tika failed to parse " + filePath, ex);
        }

        String text = handler.toString().trim();
        Map<String, String> tikaMetadata = Arrays.stream(metadata.names())
                .collect(Collectors.toMap(name -> name, metadata::get, (first, second) -> first));
        return new StructuralDocument(
                name(),
                filePath,
                fileType,
                text.isBlank() ? java.util.List.of() : java.util.List.of(StructuralElement.of(ElementType.TEXT, text)),
                tikaMetadata
        );
    }
}
