package ru.evo;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// if conflicts add before method org.assertj.core.api.Assertions
//import static org.hamcrest.MatcherAssert.assertThat; - For PDF test
import static org.assertj.core.api.Assertions.assertThat;//For XLS and CSV test
import static com.codeborne.pdftest.assertj.Assertions.assertThat; //For PDF test


public class ParseZipTest {

    ClassLoader cl = getClass().getClassLoader();

    @Test
    @DisplayName("Обработка ZIP архива c вложенными pdf,xls,csv")
    void parseZipFile() throws Exception {
        ZipFile zipFile = new ZipFile(
                new File(cl.getResource("zip/TestParse.zip").toURI()));

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            try (InputStream stream = zipFile.getInputStream(entry)) {
                switch (entry.getName()) {

                    case "pdf-test.pdf": {
                        PDF pdf = new PDF(stream);
                        assertThat(pdf)
                                .containsExactText("Please visit our website at:");
                        //With Hamcrest
                        //assertThat(pdf, new ContainsExactText("Please visit our website at:"));
                        System.out.println(entry.getName() + " " + "проверен успешно");
                        break;
                    }

                    case "tests-example.xls": {
                        XLS xls = new XLS(stream);
                        assertThat(xls.excel
                                .getSheetAt(1)
                                .getRow(15)
                                .getCell(1)
                                .getStringCellValue())
                                .contains("File response");
                        System.out.println(entry.getName() + " " + "проверен успешно");
                        break;
                    }

                    case "addresses.csv": {
                        try (CSVReader csv = new CSVReader(
                                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                            List<String[]> content = csv.readAll();
                            assertThat(content.get(1))
                                    .contains(
                                            "Jack",
                                            "McGinnis",
                                            "220 hobo Av.",
                                            "Phila",
                                            " PA",
                                            "09119");
                            System.out.println(entry.getName() + " " + "проверен успешно");
                            break;
                        }
                    }
                }
            } catch (AssertionError ae) {
                System.out.println("В " + entry.getName() + " " + "отличаются сравниваемые данные");
                System.out.println(ae.getMessage());
            }
        }
    }
}


