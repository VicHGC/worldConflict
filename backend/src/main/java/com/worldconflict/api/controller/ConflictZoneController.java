package com.worldconflict.api.controller;

import com.worldconflict.api.entity.ConflictZone;
import com.worldconflict.api.service.ConflictZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConflictZoneController {
    
    private final ConflictZoneService conflictZoneService;
    
    @GetMapping
    public ResponseEntity<List<ConflictZone>> getAllZones() {
        return ResponseEntity.ok(conflictZoneService.getAllZones());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ConflictZone> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(conflictZoneService.getZoneById(id));
    }
}
