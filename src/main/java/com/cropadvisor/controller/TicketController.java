package com.cropadvisor.controller;

import com.cropadvisor.entity.Ticket;
import com.cropadvisor.entity.TicketStatus;
import com.cropadvisor.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        Ticket ticket = ticketService.createTicket(
                request.farmerId(), request.cropName(), request.symptoms(), request.photoUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable("id") Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(@PathVariable TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/escalated")
    public ResponseEntity<List<Ticket>> getEscalatedTickets() {
        return ResponseEntity.ok(ticketService.getEscalatedTickets());
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<Ticket>> getTicketsByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(ticketService.getTicketsByRegion(regionId));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<Ticket>> getTicketsByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(ticketService.getTicketsByFarmer(farmerId));
    }

    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<Ticket>> getTicketsByOfficer(@PathVariable Long officerId) {
        return ResponseEntity.ok(ticketService.getTicketsByOfficer(officerId));
    }

    @PutMapping("/{ticketId}/recommendation/{officerId}")
    public ResponseEntity<Ticket> updateRecommendation(@PathVariable Long ticketId,
                                                        @PathVariable Long officerId,
                                                        @Valid @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(ticketService.updateTicketByOfficer(
                ticketId, officerId, request.recommendation()));
    }

    @PutMapping("/{ticketId}/close/{officerId}")
    public ResponseEntity<Ticket> closeTicket(@PathVariable Long ticketId,
                                              @PathVariable Long officerId) {
        return ResponseEntity.ok(ticketService.closeTicket(ticketId, officerId));
    }

    @PutMapping("/{ticketId}/reopen/{farmerId}")
    public ResponseEntity<Ticket> reopenTicket(@PathVariable Long ticketId,
                                               @PathVariable Long farmerId) {
        return ResponseEntity.ok(ticketService.reopenTicket(ticketId, farmerId));
    }

    public record TicketCreateRequest(
            @NotNull(message = "Farmer is required") Long farmerId,
            @NotBlank(message = "Crop name is required") String cropName,
            @NotBlank(message = "Symptoms are required") String symptoms,
            String photoUrl) {
    }

    public record RecommendationRequest(
            @NotBlank(message = "Recommendation is required") String recommendation) {
    }
}