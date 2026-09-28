package com.cropadvisor.service;

import com.cropadvisor.entity.EscalationStatus;
import com.cropadvisor.entity.Farmer;
import com.cropadvisor.entity.Officer;
import com.cropadvisor.entity.Ticket;
import com.cropadvisor.entity.TicketStatus;
import com.cropadvisor.exception.BusinessException;
import com.cropadvisor.repository.FarmerRepository;
import com.cropadvisor.repository.OfficerRepository;
import com.cropadvisor.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private static final long ESCALATION_HOURS = 48;

    private final TicketRepository ticketRepository;
    private final FarmerRepository farmerRepository;
    private final OfficerRepository officerRepository;

    public TicketService(TicketRepository ticketRepository,
                         FarmerRepository farmerRepository,
                         OfficerRepository officerRepository) {
        this.ticketRepository = ticketRepository;
        this.farmerRepository = farmerRepository;
        this.officerRepository = officerRepository;
    }

    public Ticket createTicket(Long farmerId, String cropName, String symptoms, String photoUrl) {
        Farmer farmer = getFarmer(farmerId);
        List<Officer> officers = officerRepository.findByRegionRegionId(farmer.getRegion().getRegionId());
        if (officers.isEmpty()) {
            throw new BusinessException("No officer available in region: " + farmer.getRegion().getRegionId());
        }

        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket();
        ticket.setFarmer(farmer);
        ticket.setRegion(farmer.getRegion());
        ticket.setOfficer(officers.get(0));
        ticket.setCropName(cropName);
        ticket.setSymptoms(symptoms);
        ticket.setPhotoUrl(photoUrl);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setEscalationStatus(EscalationStatus.NORMAL);
        ticket.setCreatedAt(now);
        ticket.setAssignedAt(now);
        return ticketRepository.save(ticket);
    }

    public Ticket updateTicketByOfficer(Long ticketId, Long officerId, String recommendation) {
        Ticket ticket = getTicketById(ticketId);
        Officer officer = getOfficer(officerId);
        verifyAssignedOfficer(ticket, officer);
        ticket.setRecommendation(recommendation);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return ticketRepository.save(ticket);
    }

    public Ticket closeTicket(Long ticketId, Long officerId) {
        Ticket ticket = getTicketById(ticketId);
        Officer officer = getOfficer(officerId);
        verifyAssignedOfficer(ticket, officer);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessException("Ticket is already closed: " + ticketId);
        }
        if (ticket.getRecommendation() == null || ticket.getRecommendation().isBlank()) {
            throw new BusinessException("Recommendation is required before closing ticket: " + ticketId);
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticket.setEscalationStatus(EscalationStatus.NORMAL);
        return ticketRepository.save(ticket);
    }

    public Ticket reopenTicket(Long ticketId, Long farmerId) {
        Ticket ticket = getTicketById(ticketId);
        Farmer farmer = getFarmer(farmerId);
        if (!ticket.getFarmer().getFarmerId().equals(farmer.getFarmerId())) {
            throw new BusinessException("Unauthorized farmer reopening for ticket: " + ticketId);
        }
        if (ticket.getStatus() != TicketStatus.CLOSED) {
            throw new BusinessException("Ticket is not closed and cannot be reopened: " + ticketId);
        }

        ticket.setStatus(TicketStatus.OPEN);
        ticket.setReopenedAt(LocalDateTime.now());
        ticket.setResolvedAt(null);
        ticket.setEscalationStatus(EscalationStatus.NORMAL);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> checkAndUpdateEscalations() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> tickets = ticketRepository.findAll();
        List<Ticket> changedTickets = new java.util.ArrayList<>();
        for (Ticket ticket : tickets) {
            EscalationStatus expectedStatus = calculateEscalationStatus(ticket, now);
            if (ticket.getEscalationStatus() != expectedStatus) {
                ticket.setEscalationStatus(expectedStatus);
                changedTickets.add(ticket);
            }
        }
        if (changedTickets.isEmpty()) {
            return changedTickets;
        }
        return ticketRepository.saveAll(changedTickets);
    }

    public EscalationStatus calculateEscalationStatus(Ticket ticket, LocalDateTime now) {
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            return EscalationStatus.NORMAL;
        }

        LocalDateTime timerStart = ticket.getReopenedAt() != null
                ? ticket.getReopenedAt() : ticket.getCreatedAt();
        if (timerStart != null && timerStart.plusHours(ESCALATION_HOURS).isBefore(now)) {
            return EscalationStatus.ESCALATED;
        }
        return EscalationStatus.NORMAL;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found: " + ticketId));
    }

    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> getTicketsByEscalationStatus(EscalationStatus escalationStatus) {
        return ticketRepository.findByEscalationStatus(escalationStatus);
    }

    public List<Ticket> getTicketsByRegion(Long regionId) {
        return ticketRepository.findByRegionRegionId(regionId);
    }

    public List<Ticket> getTicketsByFarmer(Long farmerId) {
        return ticketRepository.findByFarmerFarmerId(farmerId);
    }

    public List<Ticket> getTicketsByOfficer(Long officerId) {
        return ticketRepository.findByOfficerOfficerId(officerId);
    }

    public List<Ticket> getEscalatedTickets() {
        return ticketRepository.findByEscalationStatus(EscalationStatus.ESCALATED);
    }

    private Farmer getFarmer(Long farmerId) {
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new BusinessException("Farmer not found: " + farmerId));
    }

    private Officer getOfficer(Long officerId) {
        return officerRepository.findById(officerId)
                .orElseThrow(() -> new BusinessException("Officer not found: " + officerId));
    }

    private void verifyAssignedOfficer(Ticket ticket, Officer officer) {
        if (ticket.getOfficer() == null
                || !ticket.getOfficer().getOfficerId().equals(officer.getOfficerId())) {
            throw new BusinessException("Unauthorized officer action for ticket: " + ticket.getTicketId());
        }
    }
}