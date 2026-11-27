package lv.bootcamp.shelter.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JsonReportExportService {
    public void writeJsonReport(Path outputPath, ShelterReportData reportData) throws IOException {
        Map<String, Animal> old = new HashMap<>();
        Map<String, Integer> animalSpecies = new HashMap<>();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        reportData.oldBySpecies().forEach((species, optionalAnimal) -> {
            Animal animal = optionalAnimal.orElse(null);
            old.put(species, animal);
        });
        reportData.animalsBySpecies().forEach((species, animalList) -> {
            Integer animalCount = animalList.size();
            animalSpecies.put(species, animalCount);
        });
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("oldBySpecies", old);
        jsonData.put("uniqueSpecies", reportData.uniqueSpecies());
        jsonData.put("animalsNeedingVetInput", reportData.animalsNeedingVetInput());
        jsonData.put("notVaccinatedCounts", reportData.notVaccinatedCounts());
        jsonData.put("vaccinatedCounts", reportData.vaccinatedCounts());
        jsonData.put("skippedRows", reportData.importResult().skippedRows());
        jsonData.put("invalidRowNumbers", reportData.importResult().invalidRowNumbers());
        jsonData.put("totalAnimals", reportData.importResult().allAnimals().size());
        jsonData.put("animalsBySpecies", animalSpecies);

        String json = gson.toJson(jsonData);
        Files.writeString(outputPath, json, StandardCharsets.UTF_8);


    }
}
