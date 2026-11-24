package lv.bootcamp.shelter;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.service.CsvImportService;
import lv.bootcamp.shelter.service.ReportExportService;
import lv.bootcamp.shelter.service.ShelterAnalyticsService;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

@Slf4j
public class ShelterCsvApplication {

    public static void main(String[] args) {
        Path inputPath = Path.of("src", "main", "resources", "data", "intake.csv");
        Path outputPath = Path.of("output", "upload-report.txt");

        CsvImportService importService = new CsvImportService();
        ShelterAnalyticsService analyticsService = new ShelterAnalyticsService();
        ReportExportService reportExportService = new ReportExportService();

        ImportResult importResult = importService.importAnimals(inputPath);
        ShelterReportData reportData = analyticsService.buildReportData(importResult);
        try {
            reportExportService.writeReport(outputPath, reportData);
        } catch (IOException e) {
            log.error("Failed to create report: {}", outputPath, e);
            throw new UncheckedIOException(e);
        }

    }
}
