package com.saiteja.bookingservice.service.impl;

import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import com.saiteja.bookingservice.exception.ResourceNotFoundException;
import com.saiteja.bookingservice.model.Booking;
import com.saiteja.bookingservice.model.Passenger;
import com.saiteja.bookingservice.model.Ticket;
import com.saiteja.bookingservice.model.enums.BookingStatus;
import com.saiteja.bookingservice.model.enums.Gender;
import com.saiteja.bookingservice.model.enums.MealOption;
import com.saiteja.bookingservice.model.enums.TicketStatus;
import com.saiteja.bookingservice.repository.BookingRepository;
import com.saiteja.bookingservice.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTests {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Booking booking;
    private Ticket ticket;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        passenger = Passenger.builder()
                .fullName("John Doe")
                .gender(Gender.MALE)
                .age(30)
                .seatNumber("1A")
                .mealOption(MealOption.VEG)
                .build();

        booking = Booking.builder()
                .id("booking123")
                .pnr("ABC123")
                .contactEmail("test@example.com")
                .scheduleIds(List.of("schedule123"))
                .passengers(List.of(passenger))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ticket = Ticket.builder()
                .id("ticket123")
                .pnr("ABC123")
                .bookingId("booking123")
                .scheduleId("schedule123")
                .passengers(List.of(passenger))
                .status(TicketStatus.ACTIVE)
                .issuedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateTicket_shouldReturnTicketResponse_whenBookingExists() {
        // Given
        when(bookingRepository.findById("booking123")).thenReturn(Mono.just(booking));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket savedTicket = invocation.getArgument(0);
            savedTicket.setId("ticket123");
            savedTicket.setCreatedAt(LocalDateTime.now());
            savedTicket.setUpdatedAt(LocalDateTime.now());
            return Mono.just(savedTicket);
        });

        // When
        Mono<TicketResponse> result = ticketService.generateTicket("booking123");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getPnr()).isEqualTo("ABC123");
                    assertThat(response.getBookingId()).isEqualTo("booking123");
                    assertThat(response.getPassengers()).hasSize(1);
                    assertThat(response.getIssuedAt()).isNotNull();
                })
                .verifyComplete();

        verify(bookingRepository).findById("booking123");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void generateTicket_shouldThrowResourceNotFoundException_whenBookingNotFound() {
        // Given
        when(bookingRepository.findById("invalid")).thenReturn(Mono.empty());

        // When
        Mono<TicketResponse> result = ticketService.generateTicket("invalid");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Booking not found"))
                .verify();

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void generateTicket_shouldCreateTicketWithCorrectData() {
        // Given
        when(bookingRepository.findById("booking123")).thenReturn(Mono.just(booking));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket savedTicket = invocation.getArgument(0);
            assertThat(savedTicket.getPnr()).isEqualTo("ABC123");
            assertThat(savedTicket.getBookingId()).isEqualTo("booking123");
            assertThat(savedTicket.getScheduleId()).isEqualTo("schedule123");
            assertThat(savedTicket.getPassengers()).hasSize(1);
            assertThat(savedTicket.getIssuedAt()).isNotNull();
            savedTicket.setId("ticket123");
            savedTicket.setCreatedAt(LocalDateTime.now());
            savedTicket.setUpdatedAt(LocalDateTime.now());
            return Mono.just(savedTicket);
        });

        // When
        Mono<TicketResponse> result = ticketService.generateTicket("booking123");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getTicketByPnr_shouldReturnTicketResponse_whenTicketExists() {
        // Given
        when(ticketRepository.findByPnr("ABC123")).thenReturn(Mono.just(ticket));

        // When
        Mono<TicketResponse> result = ticketService.getTicketByPnr("ABC123");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getPnr()).isEqualTo("ABC123");
                    assertThat(response.getBookingId()).isEqualTo("booking123");
                    assertThat(response.getPassengers()).hasSize(1);
                    assertThat(response.getIssuedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getTicketByPnr_shouldThrowResourceNotFoundException_whenTicketNotFound() {
        // Given
        when(ticketRepository.findByPnr("INVALID")).thenReturn(Mono.empty());

        // When
        Mono<TicketResponse> result = ticketService.getTicketByPnr("INVALID");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Ticket not found"))
                .verify();
    }

    @Test
    void getTicketByPnr_shouldThrowResourceNotFoundException_whenTicketIsCancelled() {
        // Given
        ticket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findByPnr("ABC123")).thenReturn(Mono.just(ticket));

        // When
        Mono<TicketResponse> result = ticketService.getTicketByPnr("ABC123");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Ticket not found"))
                .verify();
    }

    @Test
    void getTicketByPnr_shouldReturnTicket_whenTicketIsActive() {
        // Given
        ticket.setStatus(TicketStatus.ACTIVE);
        when(ticketRepository.findByPnr("ABC123")).thenReturn(Mono.just(ticket));

        // When
        Mono<TicketResponse> result = ticketService.getTicketByPnr("ABC123");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getPnr()).isEqualTo("ABC123");
                })
                .verifyComplete();
    }
}

