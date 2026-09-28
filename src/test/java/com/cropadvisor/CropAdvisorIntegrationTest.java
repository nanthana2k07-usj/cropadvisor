package com.cropadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CropAdvisorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationHealthAndTicketList() throws Exception {

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk());
    }

    @Test
    void ticketOneShouldBeEscalated() throws Exception {

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.escalationStatus").value("ESCALATED"));
    }

    @Test
    void escalatedTicketsEndpointShouldWork() throws Exception {

        mockMvc.perform(get("/api/tickets/escalated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void unauthorizedOfficerCannotCloseTicket() throws Exception {

        mockMvc.perform(
                put("/api/tickets/1/close/2")
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void openTicketCannotBeReopened() throws Exception {

        mockMvc.perform(
                put("/api/tickets/1/reopen/1")
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void invalidFarmerCannotCreateTicket() throws Exception {

        String requestBody = """
                {
                    "farmerId": 99999,
                    "cropName": "Paddy",
                    "symptoms": "Leaves are turning yellow."
                }
                """;

        mockMvc.perform(
                post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void ticketDetailsShouldContainFarmerAndOfficer() throws Exception {

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmer.farmerId").value(1))
                .andExpect(jsonPath("$.officer.officerId").value(1))
                .andExpect(jsonPath("$.region.regionId").value(1));
    }
}