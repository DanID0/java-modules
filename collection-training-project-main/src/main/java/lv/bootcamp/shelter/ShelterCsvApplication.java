package lv.bootcamp.shelter;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.CsvImportService;
import lv.bootcamp.shelter.service.JsonReportExportService;
import lv.bootcamp.shelter.service.ReportExportService;
import lv.bootcamp.shelter.service.ShelterAnalyticsService;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class ShelterCsvApplication {

    public static void main(String[] args) {
        LocalDateTime datesAndTimes = LocalDateTime.now();
        String formatWithTime = datesAndTimes.format(DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss"));

        Path inputPath = Path.of("src", "main", "resources", "data");
        Path outputPath = Path.of("output", "upload-report-" + formatWithTime + ".txt");
        Path jsonOutputPath = Path.of("output", "upload-report-" + formatWithTime + ".json");
        Path processedFilesPath = Path.of("output", "processed-files.txt");
        Set<String> process = new HashSet<>();
        CsvImportService importService = new CsvImportService();
        ShelterAnalyticsService analyticsService = new ShelterAnalyticsService();
        ReportExportService reportExportService = new ReportExportService();
        JsonReportExportService jsonReportExportService = new JsonReportExportService();
        List<ImportResult> importResult = new ArrayList<>();
        List<Animal> animals = new ArrayList<>();
        List<Integer> invalidRowNumbers = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(inputPath)) {
            if (Files.exists(processedFilesPath)) {
                process.addAll(Files.readAllLines(processedFilesPath));
            }
            walk.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".csv") && !process.contains(inputPath.relativize(path).toString())).forEach((path) -> {
                importResult.add(importService.importAnimals(path));
                process.add(inputPath.relativize(path).toString());
            });
            importResult.forEach(result -> {
                animals.addAll(result.allAnimals());
            });
            if (importResult.isEmpty()) {
                log.info("No additional CSV file to process");
                return;
            }
            int rows = importResult.stream()
                    .mapToInt(result -> result.skippedRows())
                    .sum();
            importResult.forEach(result -> {
                invalidRowNumbers.addAll(result.invalidRowNumbers());
            });

            ImportResult combinedResult = new ImportResult(animals, rows, invalidRowNumbers);
            ShelterReportData reportData = analyticsService.buildReportData(combinedResult);
            reportExportService.writeReport(outputPath, reportData);
            jsonReportExportService.writeJsonReport(jsonOutputPath, reportData);
            Files.write(processedFilesPath, process);
        } catch (IOException e) {
            log.error("Failed to create report: {}", outputPath, e);
            throw new UncheckedIOException(e);
        }

    }
}
