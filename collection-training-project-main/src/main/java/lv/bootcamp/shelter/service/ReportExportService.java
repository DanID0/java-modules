package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportExportService {

    public void writeReport(Path outputPath, ShelterReportData reportData) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile(), StandardCharsets.UTF_8))) {
            DateTimeFormatter dates = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            writer.write("=== Shelter Intake Report ===");
            writer.newLine();
            writer.write("Date generated: " + LocalDate.now().format(dates));
            writer.newLine();
            writer.write("Total imported: " + reportData.importResult().allAnimals().size());
            writer.newLine();
            writer.write("Total skipped: " + reportData.importResult().skippedRows());
            writer.newLine();
            writer.write("Invalid: " + reportData.importResult().invalidRowNumbers().stream().map(String::valueOf).collect(Collectors.joining(", ")));
            writer.newLine();
            writer.newLine();
            writer.write("--- Unique species ---");
            writer.newLine();
            writer.write(String.join(", ", reportData.uniqueSpecies()));
            writer.newLine();
            writer.newLine();

            writer.write("--- Per-species breakdown ---");
            writer.newLine();
            for (String species : reportData.uniqueSpecies()) {
                writer.write(species + ": " + reportData.animalsBySpecies().get(species).size() + " total, " + reportData.vaccinatedCounts().getOrDefault(species, 0L) + " vaccinated");
                writer.newLine();

            }
            writer.newLine();
            writer.write("--- Oldest per species ---");
            writer.newLine();
            for (String species : reportData.uniqueSpecies()) {
                Optional<Animal> oldest = reportData.oldBySpecies().getOrDefault(species, Optional.empty());
                if (oldest.isPresent()) {
                    writer.write(species + ":  " + oldest.get().getName() + " (age " + oldest.get().getAge() + (")"));
                    writer.newLine();
                } else {
                    writer.write(species + ": Unknown age");
                    writer.newLine();
                }
            }
            writer.newLine();
            writer.write("--- Needs Vet input to determine age ---");
            writer.newLine();
            writer.write(String.join(", ", reportData.animalsNeedingVetInput()));
            writer.newLine();
        } catch (IOException e) {
            throw new IOException("Failed to write report", e);
        }
        // 1) Write upload-report.txt in required format.
        // 2) Include generated date, imported/skipped totals.
        // 3) Include unique species and per-species breakdown.
        // 4) Include oldest animal per species.
        // 5) Include animalsNeedingVetInput as name(species), name2(species2).
        // 6) Use UTF-8 and try-with-resources.
    }
}
