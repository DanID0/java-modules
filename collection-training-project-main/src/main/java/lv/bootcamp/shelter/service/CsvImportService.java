package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvImportService {

    public ImportResult importAnimals(Path inputPath) {
        log.info("Starting import from {}", inputPath);

        List<Animal> allAnimals = new ArrayList<>();
        List<Integer> invalidRowNumbers = new ArrayList<>();
        List<String> csv = new ArrayList<>();
        int skippedRows = 0;
        try {
            csv = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", inputPath, e);
            throw new UncheckedIOException(e);
        }
        for (int i = 1; i < csv.size(); i++) {
            String[] store = csv.get(i).split(",", -1);
            if (store.length != 5) {
                log.warn("Invalid CSV line: expected 5 columns, found {}", store.length);
                skippedRows += 1;
                invalidRowNumbers.add(i + 1);
                continue;
            }
            String name = store[0].trim();
            String species = store[1].trim();
            String ageText = store[2].trim();
            String vaccined = store[3].trim();
            String date = store[4].trim();
            Integer age = null;
            if (name.isBlank()) {
                log.warn("Name field cannot be empty");
                skippedRows += 1;
                invalidRowNumbers.add(i + 1);
                continue;
            }
            if (species.isBlank()) {
                log.warn("Species field cannot be empty");
                skippedRows += 1;
                invalidRowNumbers.add(i + 1);
                continue;
            }
            if (!ageText.isBlank()) {
                try {
                    age = Integer.parseInt(ageText);

                    if (age <= 0) {
                        log.warn("Skipping CSV line {}: age must be positive, like provided: '{}'", i + 1, ageText);
                        skippedRows += 1;
                        invalidRowNumbers.add(i + 1);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Skipping CSV line {}: invalid age '{}'", i + 1, ageText);
                    skippedRows += 1;
                    invalidRowNumbers.add(i + 1);
                    continue;
                }


            }
            if (!vaccined.equalsIgnoreCase("true") && !vaccined.equalsIgnoreCase("false")) {
                log.warn("Skipping CSV line {}: invalid vaccination status '{}'", i + 1, vaccined);
                skippedRows += 1;
                invalidRowNumbers.add(i + 1);
                continue;
            }
            boolean vaccinated = Boolean.parseBoolean(vaccined);
            LocalDate intakeDate;

            try {
                DateTimeFormatter dates = DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
                intakeDate = LocalDate.parse(date, dates);
            } catch (DateTimeParseException e) {
                log.warn("Skipping CSV line {}: invalid intake date '{}'", i + 1, date);
                skippedRows += 1;
                invalidRowNumbers.add(i + 1);
                continue;
            }
            allAnimals.add(new Animal(name, species, age, vaccinated, intakeDate));
        }

        // 1) Read intake.csv with UTF-8.
        // 2) Skip header row.
        // 3) Skip malformed rows and log warnings.
        // 4) Allow blank age as unknown (null), but reject non-numeric age values.
        // 5) Parse intakeDate using DateTimeFormatter.
        // 6) Map each row to Animal object.
        return new ImportResult(allAnimals, skippedRows, invalidRowNumbers);
    }
}
