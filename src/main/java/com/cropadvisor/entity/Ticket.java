package com.cropadvisor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    // Farmer who raised the ticket
    @NotNull(message = "Farmer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    // Officer automatically assigned to the ticket
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id")
    private Officer officer;

    // Region of the farmer
    @NotNull(message = "Region is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    // Crop affected
    @NotBlank(message = "Crop name is required")
    @Column(nullable = false)
    private String cropName;

    // Disease / pest symptoms
    @NotBlank(message = "Symptoms are required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String symptoms;

    // Reference/path of uploaded crop image
    private String photoUrl;

    // Current ticket status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    // Officer's recommendation
    @Column(columnDefinition = "TEXT")
    private String recommendation;

    // Ticket creation time
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Time when officer was assigned
    private LocalDateTime assignedAt;

    // Time when ticket was closed
    private LocalDateTime resolvedAt;

    // Last time farmer reopened the ticket
    private LocalDateTime reopenedAt;

    // Escalation status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscalationStatus escalationStatus;

    public Ticket() {
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = TicketStatus.OPEN;
        }

        if (escalationStatus == null) {
            escalationStatus = EscalationStatus.NORMAL;
        }
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public void setFarmer(Farmer farmer) {
        this.farmer = farmer;
    }

    public Officer getOfficer() {
        return officer;
    }

    public void setOfficer(Officer officer) {
        this.officer = officer;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getReopenedAt() {
        return reopenedAt;
    }

    public void setReopenedAt(LocalDateTime reopenedAt) {
        this.reopenedAt = reopenedAt;
    }

    public EscalationStatus getEscalationStatus() {
        return escalationStatus;
    }

    public void setEscalationStatus(EscalationStatus escalationStatus) {
        this.escalationStatus = escalationStatus;
    }
}