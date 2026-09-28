package com.cropadvisor.controller;

import com.cropadvisor.entity.Region;
import com.cropadvisor.service.RegionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PostMapping
    public ResponseEntity<Region> createRegion(@Valid @RequestBody Region region) {
        return ResponseEntity.status(HttpStatus.CREATED).body(regionService.createRegion(region));
    }

    @GetMapping
    public ResponseEntity<List<Region>> getAllRegions() {
        return ResponseEntity.ok(regionService.getAllRegions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Region> getRegionById(@PathVariable("id") Long regionId) {
        return ResponseEntity.ok(regionService.getRegionById(regionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Region> updateRegion(@PathVariable("id") Long regionId,
                                               @Valid @RequestBody Region region) {
        return ResponseEntity.ok(regionService.updateRegion(regionId, region));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable("id") Long regionId) {
        regionService.deleteRegion(regionId);
        return ResponseEntity.noContent().build();
    }
}