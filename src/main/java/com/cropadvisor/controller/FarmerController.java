package com.cropadvisor.controller;

import com.cropadvisor.entity.Farmer;
import com.cropadvisor.service.FarmerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/farmers")
public class FarmerController {

    private final FarmerService farmerService;

    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @PostMapping
    public ResponseEntity<Farmer> createFarmer(@Valid @RequestBody FarmerRequest request) {
        Farmer farmer = toFarmer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(farmerService.createFarmer(farmer, request.regionId()));
    }

    @GetMapping
    public ResponseEntity<List<Farmer>> getAllFarmers() {
        return ResponseEntity.ok(farmerService.getAllFarmers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Farmer> getFarmerById(@PathVariable("id") Long farmerId) {
        return ResponseEntity.ok(farmerService.getFarmerById(farmerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Farmer> updateFarmer(@PathVariable("id") Long farmerId,
                                               @Valid @RequestBody FarmerRequest request) {
        return ResponseEntity.ok(farmerService.updateFarmer(
                farmerId, toFarmer(request), request.regionId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarmer(@PathVariable("id") Long farmerId) {
        farmerService.deleteFarmer(farmerId);
        return ResponseEntity.noContent().build();
    }

    private Farmer toFarmer(FarmerRequest request) {
        return new Farmer(request.name(), request.phone(), request.email(), request.address(), null);
    }

    public record FarmerRequest(
            @NotBlank(message = "Farmer name is required") String name,
            @NotBlank(message = "Phone number is required") String phone,
            @Email(message = "Invalid email format") String email,
            String address,
            @NotNull(message = "Region is required") Long regionId) {
    }
}