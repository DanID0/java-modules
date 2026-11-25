package lv.bootcamp.shelter;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.service.CsvImportService;
import lv.bootcamp.shelter.service.JsonReportExportService;
import lv.bootcamp.shelter.service.ReportExportService;
import lv.bootcamp.shelter.service.ShelterAnalyticsService;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ShelterCsvApplication {

    public static void main(String[] args) {
        LocalDateTime datesAndTimes = LocalDateTime.now();
        String formatWithTime = datesAndTimes.format(DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss"));

        Path inputPath = Path.of("src", "main", "resources", "data", "intake.csv");
        Path outputPath = Path.of("output", "upload-report-" + formatWithTime + ".txt");
        Path jsonOutputPath = Path.of("output", "upload-report-" + formatWithTime + ".json");
        CsvImportService importService = new CsvImportService();
        ShelterAnalyticsService analyticsService = new ShelterAnalyticsService();
        ReportExportService reportExportService = new ReportExportService();
        JsonReportExportService jsonReportExportService = new JsonReportExportService();

        ImportResult importResult = importService.importAnimals(inputPath);
        ShelterReportData reportData = analyticsService.buildReportData(importResult);
        try {
            reportExportService.writeReport(outputPath, reportData);
            jsonReportExportService.writeJsonReport(jsonOutputPath, reportData);
        } catch (IOException e) {
            log.error("Failed to create report: {}", outputPath, e);
            throw new UncheckedIOException(e);
        }

    }
}
