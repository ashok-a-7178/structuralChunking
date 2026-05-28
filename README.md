# structuralChunking

Java-based raw and structural file parsing demo for comparing parser libraries across multiple file formats.

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

The dataset runner parses each file with the built-in parser and Apache Tika, chunks the resulting structural data with the same chunker, and prints validation scores for head-to-head comparison.

Optional command-line adapters can be enabled for parsers that are not Java libraries:
- `STRUCTURAL_UNSTRUCTURED_COMMAND`
- `STRUCTURAL_DOCLING_COMMAND`

Each command should accept the file path as its final argument and print extracted text to standard output.
