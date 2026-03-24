package com.worldconflict.api.service;

import com.worldconflict.api.entity.ConflictZone;
import com.worldconflict.api.repository.ConflictZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConflictZoneService implements CommandLineRunner {
    
    private final ConflictZoneRepository conflictZoneRepository;
    
    @Override
    public void run(String... args) {
        if (conflictZoneRepository.count() == 0) {
            initializeZones();
        }
    }
    
    private void initializeZones() {
        log.info("Initializing conflict zones...");
        
        List<ConflictZone> zones = List.of(
            createZone("Ukraine", 48.3794, 31.1656, "Active conflict zone in Eastern Europe", "Europe"),
            createZone("Iran", 32.4279, 53.6880, "Middle East tensions and nuclear program", "Middle East"),
            createZone("Israel-Gaza", 31.0461, 34.8516, "Ongoing conflict in Gaza strip", "Middle East"),
            createZone("Syria", 34.8021, 38.9968, "Civil war and regional tensions", "Middle East"),
            createZone("Yemen", 15.5527, 48.9934, "Humanitarian crisis and conflict", "Middle East"),
            createZone("Afghanistan", 33.9391, 67.7099, "Security concerns and instability", "Asia"),
            createZone("Myanmar", 21.9162, 95.9560, "Political crisis and conflict", "Asia"),
            createZone("Ethiopia", 9.1450, 40.4897, "Regional conflicts and humanitarian issues", "Africa"),
            createZone("Sudan", 12.8628, 30.2176, "Ongoing civil conflict", "Africa"),
            createZone("South Sudan", 6.8770, 31.3070, "Humanitarian and security situation", "Africa"),
            createZone("Haiti", 18.9712, -72.2852, "Gang violence and political instability", "Americas"),
            createZone("Venezuela", 6.4238, -66.5897, "Economic and political crisis", "Americas"),
            createZone("North Korea", 40.3399, 127.5101, "Nuclear tensions and isolation", "Asia"),
            createZone("Iraq", 33.2232, 43.6793, "Political instability and security concerns", "Middle East"),
            createZone("Libya", 26.3351, 17.2283, "Civil conflict and fragmentation", "Africa"),
            createZone("Somalia", 5.1521, 46.1996, "Al-Shabaab insurgency and humanitarian crisis", "Africa"),
            createZone("Democratic Republic of Congo", -4.0383, 21.7587, "Armed conflicts and resource wars", "Africa"),
            createZone("Mali", 17.5707, -3.9962, "Jihadist insurgency and instability", "Africa"),
            createZone("Nagorno-Karabakh", 39.9064, 46.6532, "Armenia-Azerbaijan tensions", "Europe"),
            createZone("Honduras", 15.2000, -86.2419, "Gang violence and political instability", "Americas")
        );
        
        conflictZoneRepository.saveAll(zones);
        log.info("Initialized {} conflict zones", zones.size());
    }
    
    private ConflictZone createZone(String name, double lat, double lng, String description, String region) {
        ConflictZone zone = new ConflictZone();
        zone.setName(name);
        zone.setLatitude(lat);
        zone.setLongitude(lng);
        zone.setDescription(description);
        zone.setRegion(region);
        zone.setIsActive(true);
        return zone;
    }
    
    public List<ConflictZone> getAllZones() {
        return conflictZoneRepository.findByIsActive(true);
    }
    
    public ConflictZone getZoneById(Long id) {
        return conflictZoneRepository.findById(id).orElse(null);
    }
}
