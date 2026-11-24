package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.util.*;
import java.util.stream.Collectors;

public class ShelterAnalyticsService {

    public ShelterReportData buildReportData(ImportResult importResult) {
        List<Animal> allAnimals = importResult.allAnimals();

        Set<String> uniqueSpecies = new TreeSet<>();
        Map<String, List<Animal>> animalsBySpecies = new HashMap<>();
        List<String> animalsNeedingVetInput = new ArrayList<>();
        Map<String, Long> vaccinatedCounts = new HashMap<>();
        Map<String, Long> notVaccinatedCounts = new HashMap<>();
        Map<String, Optional<Animal>> oldBySpecies = new HashMap<>();
        for (int i = 0; i < allAnimals.size(); i++) {
            uniqueSpecies.add(allAnimals.get(i).getSpecies());

            if (!animalsBySpecies.containsKey(allAnimals.get(i).getSpecies())) {
                ArrayList<Animal> animalsBySpeciesR = new ArrayList<>();
                animalsBySpecies.put(allAnimals.get(i).getSpecies(), animalsBySpeciesR);
            }
            animalsBySpecies.get(allAnimals.get(i).getSpecies()).add(allAnimals.get(i));
            if (allAnimals.get(i).getAge() == null) {
                animalsNeedingVetInput.add((allAnimals.get(i)).getName() + "(" + allAnimals.get(i).getSpecies() + ")");
            }
        }
        vaccinatedCounts = allAnimals.stream().filter(Animal::isVaccinated).collect(Collectors.groupingBy(Animal::getSpecies, Collectors.counting()));
        notVaccinatedCounts = allAnimals.stream().filter(animal -> !animal.isVaccinated()).collect(Collectors.groupingBy(Animal::getSpecies, Collectors.counting()));
        oldBySpecies = allAnimals.stream().filter(animal -> animal.getAge() != null).collect(Collectors.groupingBy(Animal::getSpecies, Collectors.maxBy(Comparator.comparingInt(Animal::getAge))));
        
        return new ShelterReportData(importResult, uniqueSpecies, animalsBySpecies, animalsNeedingVetInput, vaccinatedCounts, notVaccinatedCounts, oldBySpecies);
    }
}
