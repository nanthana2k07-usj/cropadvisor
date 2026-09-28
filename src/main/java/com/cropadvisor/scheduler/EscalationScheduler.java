package com.cropadvisor.scheduler;

import com.cropadvisor.service.TicketService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EscalationScheduler {

    private static final long ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000L;

    private final TicketService ticketService;

    public EscalationScheduler(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Scheduled(fixedRate = ONE_HOUR_IN_MILLISECONDS)
    public void checkEscalations() {
        ticketService.checkAndUpdateEscalations();
    }
}