package com.structuralchunking.extractor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

public class RawFileTextExtractor {

    public String extractText(Path filePath) throws IOException {
        FileType fileType = FileTypeDetector.detect(filePath);

        return switch (fileType) {
            case PDF -> extractPdf(filePath);
            case HTML -> extractHtml(filePath);
            case CSV -> extractCsv(filePath);
            case XLS, XLSX -> extractSpreadsheet(filePath);
            case PPT, PPTX -> extractPresentation(filePath);
            case UNKNOWN -> throw new IllegalArgumentException("Unsupported file type for file: " + filePath);
        };
    }

    private String extractPdf(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            return new PDFTextStripper().getText(document).trim();
        }
    }

    private String extractHtml(Path filePath) throws IOException {
        return Jsoup.parse(filePath.toFile(), StandardCharsets.UTF_8.name()).text();
    }

    private String extractCsv(Path filePath) throws IOException {
        StringJoiner lines = new StringJoiner(System.lineSeparator());
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            for (CSVRecord record : CSVFormat.DEFAULT.parse(reader)) {
                StringJoiner columns = new StringJoiner(" ");
                for (String value : record) {
                    if (!value.isBlank()) {
                        columns.add(value.trim());
                    }
                }
                String line = columns.toString().trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines.toString();
    }

    private String extractSpreadsheet(Path filePath) throws IOException {
        DataFormatter formatter = new DataFormatter();
        StringJoiner text = new StringJoiner(System.lineSeparator());
        try (Workbook workbook = WorkbookFactory.create(filePath.toFile())) {
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    StringJoiner rowText = new StringJoiner(" ");
                    for (Cell cell : row) {
                        String value = formatter.formatCellValue(cell).trim();
                        if (!value.isEmpty()) {
                            rowText.add(value);
                        }
                    }
                    if (!rowText.toString().isEmpty()) {
                        text.add(rowText.toString());
                    }
                }
            }
        }
        return text.toString();
    }

    private String extractPresentation(Path filePath) throws IOException {
        try (SlideShow<?, ?> slideShow = SlideShowFactory.create(filePath.toFile());
             SlideShowExtractor<?, ?> extractor = new SlideShowExtractor<>(slideShow)) {
            return extractor.getText().trim();
        }
    }
}
