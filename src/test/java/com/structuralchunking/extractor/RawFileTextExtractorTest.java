package com.structuralchunking.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextBox;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RawFileTextExtractorTest {

    private final RawFileTextExtractor extractor = new RawFileTextExtractor();

    @TempDir
    Path tempDir;

    @Test
    void extractsFromHtmlCsvPdfXlsXlsxPptPptx() throws Exception {
        Path html = createHtml();
        Path csv = createCsv();
        Path pdf = createPdf("sample.bin");
        Path xls = createXls();
        Path xlsx = createXlsx();
        Path ppt = createPpt();
        Path pptx = createPptx();

        assertTrue(extractor.extractText(html).contains("Hello HTML"));
        assertTrue(extractor.extractText(csv).contains("Alice"));
        assertTrue(extractor.extractText(pdf).contains("Hello PDF"));
        assertTrue(extractor.extractText(xls).contains("Hello XLS"));
        assertTrue(extractor.extractText(xlsx).contains("Hello XLSX"));
        assertTrue(extractor.extractText(ppt).contains("Hello PPT"));
        assertTrue(extractor.extractText(pptx).contains("Hello PPTX"));
    }

    private Path createHtml() throws IOException {
        Path file = tempDir.resolve("sample.html");
        Files.writeString(file, "<html><body><h1>Hello HTML</h1></body></html>", StandardCharsets.UTF_8);
        return file;
    }

    private Path createCsv() throws IOException {
        Path file = tempDir.resolve("sample.csv");
        Files.writeString(file, "name,city\nAlice,Seattle\n", StandardCharsets.UTF_8);
        return file;
    }

    private Path createPdf(String fileName) throws IOException {
        Path file = tempDir.resolve(fileName);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(100, 700);
                stream.showText("Hello PDF");
                stream.endText();
            }
            document.save(file.toFile());
        }
        return file;
    }

    private Path createXls() throws IOException {
        Path file = tempDir.resolve("sample.xls");
        try (Workbook workbook = new HSSFWorkbook(); OutputStream out = Files.newOutputStream(file)) {
            workbook.createSheet("Data").createRow(0).createCell(0).setCellValue("Hello XLS");
            workbook.write(out);
        }
        return file;
    }

    private Path createXlsx() throws IOException {
        Path file = tempDir.resolve("sample.xlsx");
        try (Workbook workbook = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(file)) {
            workbook.createSheet("Data").createRow(0).createCell(0).setCellValue("Hello XLSX");
            workbook.write(out);
        }
        return file;
    }

    private Path createPpt() throws IOException {
        Path file = tempDir.resolve("sample.ppt");
        try (HSLFSlideShow slideShow = new HSLFSlideShow(); OutputStream out = Files.newOutputStream(file)) {
            HSLFSlide slide = slideShow.createSlide();
            HSLFTextBox box = new HSLFTextBox();
            box.setText("Hello PPT");
            slide.addShape(box);
            slideShow.write(out);
        }
        return file;
    }

    private Path createPptx() throws IOException {
        Path file = tempDir.resolve("sample.pptx");
        try (XMLSlideShow slideShow = new XMLSlideShow(); OutputStream out = Files.newOutputStream(file)) {
            XSLFSlide slide = slideShow.createSlide();
            XSLFTextBox box = slide.createTextBox();
            box.setText("Hello PPTX");
            slideShow.write(out);
        }
        return file;
    }
}
