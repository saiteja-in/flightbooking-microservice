package com.saiteja.bookingservice.controller;

import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import com.saiteja.bookingservice.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTests {

    private WebTestClient webTestClient;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(ticketController).build();
    }

    @Test
    void getTicketByPnr_shouldReturnOk_whenTicketExists() {
        // Given
        TicketResponse ticketResponse = TicketResponse.builder()
                .pnr("ABC123")
                .bookingId("booking123")
                .passengers(List.of())
                .issuedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(ticketService.getTicketByPnr("ABC123"))
                .thenReturn(Mono.just(ticketResponse));

        // When & Then
        webTestClient.get()
                .uri("/api/v1.0/flight/ticket/ABC123")
                .exchange()
                .expectStatus().isOk();
    }
}

