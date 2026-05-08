package com.structuralchunking.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;

public final class FileTypeDetector {
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46};
    private static final byte[] OLE2_MAGIC = new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0};
    private static final byte[] ZIP_MAGIC = new byte[]{0x50, 0x4B, 0x03, 0x04};
    private static final int TEXT_SAMPLE_BYTES = 8192;

    private FileTypeDetector() {
    }

    public static FileType detect(Path filePath) throws IOException {
        byte[] bytes = readPrefix(filePath, TEXT_SAMPLE_BYTES);
        String extension = extensionOf(filePath);

        if (startsWith(bytes, PDF_MAGIC)) {
            return FileType.PDF;
        }
        if (startsWith(bytes, OLE2_MAGIC)) {
            if ("xls".equals(extension)) {
                return FileType.XLS;
            }
            if ("ppt".equals(extension)) {
                return FileType.PPT;
            }
        }
        if (startsWith(bytes, ZIP_MAGIC)) {
            if ("xlsx".equals(extension)) {
                return FileType.XLSX;
            }
            if ("pptx".equals(extension)) {
                return FileType.PPTX;
            }
        }

        String content = new String(bytes, StandardCharsets.UTF_8).toLowerCase();
        if ("html".equals(extension) || content.contains("<html") || content.contains("<!doctype html")) {
            return FileType.HTML;
        }
        if ("csv".equals(extension) || content.contains(",") && content.contains("\n")) {
            return FileType.CSV;
        }
        return FileType.UNKNOWN;
    }

    private static String extensionOf(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] readPrefix(Path filePath, int maxBytes) throws IOException {
        byte[] buffer = new byte[maxBytes];
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            int read = inputStream.read(buffer);
            if (read <= 0) {
                return new byte[0];
            }
            return Arrays.copyOf(buffer, read);
        }
    }
}
