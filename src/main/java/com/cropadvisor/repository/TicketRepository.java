package com.cropadvisor.repository;

import com.cropadvisor.entity.EscalationStatus;
import com.cropadvisor.entity.Ticket;
import com.cropadvisor.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByEscalationStatus(EscalationStatus escalationStatus);

    List<Ticket> findByRegionRegionId(Long regionId);

    List<Ticket> findByFarmerFarmerId(Long farmerId);

    List<Ticket> findByOfficerOfficerId(Long officerId);
}