package com.saiteja.bookingservice.service.impl;

import com.saiteja.bookingservice.client.FlightServiceClient;
import com.saiteja.bookingservice.dto.booking.BookingCreateRequest;
import com.saiteja.bookingservice.dto.booking.BookingResponse;
import com.saiteja.bookingservice.dto.passenger.PassengerRequest;
import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import com.saiteja.bookingservice.exception.BadRequestException;
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
import com.saiteja.bookingservice.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private FlightServiceClient flightServiceClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingCreateRequest bookingRequest;
    private Booking savedBooking;
    private TicketResponse ticketResponse;
    private PassengerRequest passengerRequest;

    @BeforeEach
    void setUp() {
        passengerRequest = new PassengerRequest();
        passengerRequest.setFullName("John Doe");
        passengerRequest.setGender(Gender.MALE);
        passengerRequest.setAge(30);
        passengerRequest.setSeatNumber("1A");
        passengerRequest.setMealOption(MealOption.VEG);

        bookingRequest = new BookingCreateRequest();
        bookingRequest.setContactEmail("test@example.com");
        bookingRequest.setScheduleIds(List.of("schedule123"));
        bookingRequest.setPassengers(List.of(passengerRequest));

        Passenger passenger = Passenger.builder()
                .fullName("John Doe")
                .gender(Gender.MALE)
                .age(30)
                .seatNumber("1A")
                .mealOption(MealOption.VEG)
                .build();

        savedBooking = Booking.builder()
                .id("booking123")
                .pnr("ABC123")
                .contactEmail("test@example.com")
                .scheduleIds(List.of("schedule123"))
                .passengers(List.of(passenger))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ticketResponse = TicketResponse.builder()
                .pnr("ABC123")
                .bookingId("booking123")
                .passengers(List.of())
                .issuedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createBooking_shouldReturnTicketResponse_whenValidRequest() {
        // Given
        when(flightServiceClient.lockSeats(anyString(), anyList())).thenReturn(Mono.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(savedBooking));
        when(ticketService.generateTicket(anyString())).thenReturn(Mono.just(ticketResponse));

        // When
        Mono<TicketResponse> result = bookingService.createBooking(bookingRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getPnr()).isEqualTo("ABC123");
                })
                .verifyComplete();

        verify(flightServiceClient).lockSeats(eq("schedule123"), anyList());
        verify(bookingRepository).save(any(Booking.class));
        verify(ticketService).generateTicket("booking123");
    }

    @Test
    void createBooking_shouldThrowBadRequestException_whenScheduleIdsIsNull() {
        // Given
        bookingRequest.setScheduleIds(null);

        // When
        Mono<TicketResponse> result = bookingService.createBooking(bookingRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().equals("At least one schedule id is required"))
                .verify();

        verify(flightServiceClient, never()).lockSeats(anyString(), anyList());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowBadRequestException_whenScheduleIdsIsEmpty() {
        // Given
        bookingRequest.setScheduleIds(List.of());

        // When
        Mono<TicketResponse> result = bookingService.createBooking(bookingRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().equals("At least one schedule id is required"))
                .verify();

        verify(flightServiceClient, never()).lockSeats(anyString(), anyList());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowBadRequestException_whenPassengersIsEmpty() {
        // Given
        bookingRequest.setPassengers(List.of());

        // When
        Mono<TicketResponse> result = bookingService.createBooking(bookingRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().equals("At least one passenger is required"))
                .verify();

        verify(flightServiceClient, never()).lockSeats(anyString(), anyList());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookingByPnr_shouldReturnBookingResponse_whenBookingExists() {
        // Given
        when(bookingRepository.findByPnr("ABC123")).thenReturn(Mono.just(savedBooking));

        // When
        Mono<BookingResponse> result = bookingService.getBookingByPnr("ABC123");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getPnr()).isEqualTo("ABC123");
                    assertThat(response.getContactEmail()).isEqualTo("test@example.com");
                    assertThat(response.getStatus()).isEqualTo("CONFIRMED");
                    assertThat(response.getPassengers()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    void getBookingByPnr_shouldThrowResourceNotFoundException_whenBookingNotFound() {
        // Given
        when(bookingRepository.findByPnr("INVALID")).thenReturn(Mono.empty());

        // When
        Mono<BookingResponse> result = bookingService.getBookingByPnr("INVALID");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Booking not found"))
                .verify();
    }

    @Test
    void getBookingsByEmail_shouldReturnFluxOfBookingResponses() {
        // Given
        Booking booking2 = Booking.builder()
                .id("booking456")
                .pnr("XYZ789")
                .contactEmail("test@example.com")
                .scheduleIds(List.of("schedule456"))
                .passengers(List.of())
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(bookingRepository.findByContactEmail("test@example.com"))
                .thenReturn(Flux.just(savedBooking, booking2));

        // When
        Flux<BookingResponse> result = bookingService.getBookingsByEmail("test@example.com");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getPnr()).isEqualTo("ABC123");
                })
                .assertNext(response -> {
                    assertThat(response.getPnr()).isEqualTo("XYZ789");
                })
                .verifyComplete();
    }

    @Test
    void getBookingsByEmail_shouldReturnEmptyFlux_whenNoBookingsFound() {
        // Given
        when(bookingRepository.findByContactEmail("test@example.com")).thenReturn(Flux.empty());

        // When
        Flux<BookingResponse> result = bookingService.getBookingsByEmail("test@example.com");

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void cancelBooking_shouldReturnSuccessMessage_whenBookingExists() {
        // Given
        Ticket ticket = Ticket.builder()
                .id("ticket123")
                .pnr("ABC123")
                .bookingId("booking123")
                .scheduleId("schedule123")
                .status(TicketStatus.ACTIVE)
                .passengers(List.of())
                .issuedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(bookingRepository.findByPnr("ABC123")).thenReturn(Mono.just(savedBooking));
        when(flightServiceClient.releaseSeats(anyString(), anyList())).thenReturn(Mono.empty());
        when(ticketRepository.findByPnr("ABC123")).thenReturn(Mono.just(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(Mono.just(ticket));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(savedBooking));

        // When
        Mono<String> result = bookingService.cancelBooking("ABC123");

        // Then
        StepVerifier.create(result)
                .assertNext(message -> {
                    assertThat(message).isEqualTo("Booking and Ticket Cancelled");
                })
                .verifyComplete();

        verify(flightServiceClient).releaseSeats(eq("schedule123"), anyList());
        verify(ticketRepository).findByPnr("ABC123");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void cancelBooking_shouldThrowResourceNotFoundException_whenBookingNotFound() {
        // Given
        when(bookingRepository.findByPnr("INVALID")).thenReturn(Mono.empty());

        // When
        Mono<String> result = bookingService.cancelBooking("INVALID");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Booking not found"))
                .verify();

        verify(flightServiceClient, never()).releaseSeats(anyString(), anyList());
    }

    @Test
    void cancelBooking_shouldThrowBadRequestException_whenBookingAlreadyCancelled() {
        // Given
        savedBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByPnr("ABC123")).thenReturn(Mono.just(savedBooking));

        // When
        Mono<String> result = bookingService.cancelBooking("ABC123");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().equals("Booking already cancelled"))
                .verify();

        verify(flightServiceClient, never()).releaseSeats(anyString(), anyList());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_shouldHandleTicketNotFoundGracefully() {
        // Given
        when(bookingRepository.findByPnr("ABC123")).thenReturn(Mono.just(savedBooking));
        when(flightServiceClient.releaseSeats(anyString(), anyList())).thenReturn(Mono.empty());
        when(ticketRepository.findByPnr("ABC123")).thenReturn(Mono.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(savedBooking));

        // When
        Mono<String> result = bookingService.cancelBooking("ABC123");

        // Then
        StepVerifier.create(result)
                .assertNext(message -> {
                    assertThat(message).isEqualTo("Booking and Ticket Cancelled");
                })
                .verifyComplete();

        verify(flightServiceClient).releaseSeats(anyString(), anyList());
        verify(bookingRepository).save(any(Booking.class));
    }
}

