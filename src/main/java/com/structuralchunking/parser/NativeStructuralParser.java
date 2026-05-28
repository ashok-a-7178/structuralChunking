package com.structuralchunking.parser;

import com.structuralchunking.extractor.FileType;
import com.structuralchunking.extractor.FileTypeDetector;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class NativeStructuralParser implements StructuralParser {
    private static final String HTML_STRUCTURAL_ELEMENTS = "h1,h2,h3,h4,h5,h6,p,li,tr";

    @Override
    public String name() {
        return "native";
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

        List<StructuralElement> elements = switch (fileType) {
            case PDF -> parsePdf(filePath);
            case HTML -> parseHtml(filePath);
            case CSV -> parseCsv(filePath);
            case XLS, XLSX -> parseSpreadsheet(filePath);
            case PPT, PPTX -> parsePresentation(filePath);
            case UNKNOWN -> List.of();
        };
        return new StructuralDocument(name(), filePath, fileType, elements, Map.of());
    }

    private List<StructuralElement> parsePdf(Path filePath) throws IOException {
        List<StructuralElement> elements = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(document).trim();
                if (!text.isBlank()) {
                    elements.add(new StructuralElement(ElementType.PAGE, text, page, Map.of("page", Integer.toString(page))));
                }
            }
        }
        return elements;
    }

    private List<StructuralElement> parseHtml(Path filePath) throws IOException {
        Document document = Jsoup.parse(filePath.toFile(), StandardCharsets.UTF_8.name());
        List<StructuralElement> elements = new ArrayList<>();
        if (!document.title().isBlank()) {
            elements.add(StructuralElement.of(ElementType.TITLE, document.title()));
        }
        for (Element element : document.body().select(HTML_STRUCTURAL_ELEMENTS)) {
            String text = element.text().trim();
            if (text.isBlank()) {
                continue;
            }
            String tag = element.tagName();
            if (tag.matches("h[1-6]")) {
                elements.add(StructuralElement.of(ElementType.HEADING, text, Integer.parseInt(tag.substring(1))));
            } else if ("li".equals(tag)) {
                elements.add(StructuralElement.of(ElementType.LIST_ITEM, text));
            } else if ("tr".equals(tag)) {
                elements.add(StructuralElement.of(ElementType.TABLE_ROW, text));
            } else {
                elements.add(StructuralElement.of(ElementType.PARAGRAPH, text));
            }
        }
        return elements;
    }

    private List<StructuralElement> parseCsv(Path filePath) throws IOException {
        List<StructuralElement> elements = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            for (CSVRecord record : CSVFormat.DEFAULT.parse(reader)) {
                String line = joinValues(record);
                if (!line.isBlank()) {
                    elements.add(new StructuralElement(ElementType.TABLE_ROW, line, 0, Map.of("row", Long.toString(record.getRecordNumber()))));
                }
            }
        }
        return elements;
    }

    private List<StructuralElement> parseSpreadsheet(Path filePath) throws IOException {
        DataFormatter formatter = new DataFormatter();
        List<StructuralElement> elements = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(filePath.toFile())) {
            for (Sheet sheet : workbook) {
                elements.add(new StructuralElement(ElementType.SHEET, sheet.getSheetName(), 0, Map.of("sheet", sheet.getSheetName())));
                for (Row row : sheet) {
                    StringJoiner rowText = new StringJoiner(" ");
                    for (Cell cell : row) {
                        String value = formatter.formatCellValue(cell).trim();
                        if (!value.isEmpty()) {
                            rowText.add(value);
                        }
                    }
                    if (!rowText.toString().isBlank()) {
                        elements.add(new StructuralElement(
                                ElementType.TABLE_ROW,
                                rowText.toString(),
                                0,
                                Map.of("sheet", sheet.getSheetName(), "row", Integer.toString(row.getRowNum() + 1))
                        ));
                    }
                }
            }
        }
        return elements;
    }

    private List<StructuralElement> parsePresentation(Path filePath) throws IOException {
        try (SlideShow<?, ?> slideShow = SlideShowFactory.create(filePath.toFile());
             SlideShowExtractor<?, ?> extractor = new SlideShowExtractor<>(slideShow)) {
            String text = extractor.getText().trim();
            if (text.isBlank()) {
                return List.of();
            }
            return List.of(StructuralElement.of(ElementType.SLIDE, text));
        }
    }

    private String joinValues(CSVRecord record) {
        StringJoiner columns = new StringJoiner(" ");
        for (String value : record) {
            if (!value.isBlank()) {
                columns.add(value.trim());
            }
        }
        return columns.toString();
    }
}
