package com.cropadvisor.controller;

import com.cropadvisor.entity.Officer;
import com.cropadvisor.service.OfficerService;
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
@RequestMapping("/api/officers")
public class OfficerController {

    private final OfficerService officerService;

    public OfficerController(OfficerService officerService) {
        this.officerService = officerService;
    }

    @PostMapping
    public ResponseEntity<Officer> createOfficer(@Valid @RequestBody OfficerRequest request) {
        Officer officer = toOfficer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(officerService.createOfficer(officer, request.regionId()));
    }

    @GetMapping
    public ResponseEntity<List<Officer>> getAllOfficers() {
        return ResponseEntity.ok(officerService.getAllOfficers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Officer> getOfficerById(@PathVariable("id") Long officerId) {
        return ResponseEntity.ok(officerService.getOfficerById(officerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Officer> updateOfficer(@PathVariable("id") Long officerId,
                                                 @Valid @RequestBody OfficerRequest request) {
        return ResponseEntity.ok(officerService.updateOfficer(
                officerId, toOfficer(request), request.regionId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfficer(@PathVariable("id") Long officerId) {
        officerService.deleteOfficer(officerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<Officer>> getOfficersByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(officerService.getOfficersByRegion(regionId));
    }

    private Officer toOfficer(OfficerRequest request) {
        return new Officer(request.name(), request.phone(), request.email(),
                request.specialization(), null);
    }

    public record OfficerRequest(
            @NotBlank(message = "Officer name is required") String name,
            @NotBlank(message = "Phone number is required") String phone,
            @Email(message = "Invalid email format") String email,
            @NotBlank(message = "Specialization is required") String specialization,
            @NotNull(message = "Region is required") Long regionId) {
    }
}