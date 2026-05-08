# structuralChunking

Java-based raw file text extraction demo for multiple file formats.

## Supported formats
- PDF
- HTML
- CSV
- XLS / XLSX
- PPT / PPTX

## Run tests
```bash
mvn test
```

## Run dataset extraction
```bash
mvn -q exec:java -Dexec.mainClass="com.structuralchunking.extractor.DatasetExtractionApp" -Dexec.args="/path/to/dataset"
```
