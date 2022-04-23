package ru.evo;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


public class DownloadnCheckTest {

    ClassLoader cl = getClass().getClassLoader(); // or 2nd example DownloadnCheckTest.class.getClassLoader();

    @Test
    @DisplayName("Первый пробный тест скачать какой-то файл")
    void downloadTest() throws Exception {
        open("https://github.com/qa-guru/knowledge-base/blob/main/README.md");
        File textFile = $("#raw-url").download(); // без href в DOM элементе не скачает
        try (InputStream is = new FileInputStream(textFile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            com.codeborne.pdftest.assertj.Assertions.assertThat(strContent).contains("JUnit 5");
        }
    }

    @Test
    @DisplayName("Обработка PDF файла")
    void parsePdfFile() throws Exception {
        InputStream stream = cl.getResourceAsStream("pdf/pdf-test.pdf");
        PDF pdf = new PDF(stream);
        Assertions.assertEquals(1, pdf.numberOfPages);
        assertThat(pdf, new ContainsExactText("Yukon Department"));
    }

    @Test
    @DisplayName("Обработка XLS файла")
    void parseXlsFile() throws Exception {
        InputStream stream = cl.getResourceAsStream("xls/tests-example.xls");
        XLS xls = new XLS(stream);
        String stringCellValue = xls.excel
                .getSheetAt(0)
                .getRow(4)
                .getCell(2)
                .getStringCellValue();
        org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("Parentheses");
    }

    @Test
    @DisplayName("Обработка CSV файла")
    void parseCsvFile() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("csv/test-address.csv");
             CSVReader reader = new CSVReader(new InputStreamReader
                     (stream, StandardCharsets.UTF_8))) {

            // Чтение одной строки
            //            List<String[]> content = reader.readAll();
            //            org.assertj.core.api.Assertions.assertThat(content.get(0)).contains(
            //            "John", "Doe", "120 jefferson st.", "Riverside", "NJ", "08075");

            List<String[]> content = reader.readAll();
            org.assertj.core.api.Assertions.assertThat(content).contains(
                    new String[]{"John", "Doe", "120 jefferson st.",
                            "Riverside", "NJ", "08075"},
                    new String[]{"Jack", "McGinnis", "220 hobo Av.",
                            "Phila", "PA", "09119"}
            );
        }
    }

    @Test
    @DisplayName("Обработка ZIP архива")
    void parseZipFile() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/zip/zip-example.zip"));
        ZipInputStream is = new ZipInputStream(cl.getResourceAsStream("zip/zip-example.zip"));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            org.assertj.core.api.Assertions.assertThat(entry
                            .getName())
                    .isEqualTo("tests-example.xls");
            InputStream inputStream = zf.getInputStream(entry);
        }
    }
}
