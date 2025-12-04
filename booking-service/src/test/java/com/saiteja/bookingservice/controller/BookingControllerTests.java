package com.saiteja.bookingservice.controller;

import com.saiteja.bookingservice.dto.ApiResponse;
import com.saiteja.bookingservice.dto.booking.BookingCreateRequest;
import com.saiteja.bookingservice.dto.passenger.PassengerRequest;
import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import com.saiteja.bookingservice.model.enums.Gender;
import com.saiteja.bookingservice.model.enums.MealOption;
import com.saiteja.bookingservice.service.BookingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTests {

    private WebTestClient webTestClient;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(bookingController).build();
    }

    @Test
    void bookFlight_shouldReturnCreated_whenValidRequest() {
        // Given
        BookingCreateRequest req = new BookingCreateRequest();
        req.setContactEmail("test@example.com");
        PassengerRequest p = new PassengerRequest();
        p.setFullName("John Doe");
        p.setGender(Gender.MALE);
        p.setAge(30);
        p.setSeatNumber("1A");
        p.setMealOption(MealOption.VEG);
        req.setPassengers(List.of(p));

        TicketResponse ticketResponse = TicketResponse.builder()
                .pnr("ABC123")
                .bookingId("booking123")
                .passengers(List.of())
                .issuedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(bookingService.createBooking(any(BookingCreateRequest.class)))
                .thenReturn(Mono.just(ticketResponse));

        // When & Then
        webTestClient.post()
                .uri("/api/v1.0/flight/booking/abc123")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void cancelBooking_shouldReturnOk_whenValidPnr() {
        // Given
        when(bookingService.cancelBooking("ABC123"))
                .thenReturn(Mono.just("Booking and Ticket Cancelled"));

        // When & Then
        webTestClient.delete()
                .uri("/api/v1.0/flight/booking/cancel/ABC123")
                .exchange()
                .expectStatus().isOk();
    }
}


