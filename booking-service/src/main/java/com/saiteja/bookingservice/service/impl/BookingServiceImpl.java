package com.saiteja.bookingservice.service.impl;

import com.saiteja.bookingservice.client.FlightServiceClient;
import com.saiteja.bookingservice.dto.booking.BookingCreateRequest;
import com.saiteja.bookingservice.dto.booking.BookingResponse;
import com.saiteja.bookingservice.dto.passenger.PassengerResponse;
import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import com.saiteja.bookingservice.exception.BadRequestException;
import com.saiteja.bookingservice.exception.ResourceNotFoundException;
import com.saiteja.bookingservice.model.Booking;
import com.saiteja.bookingservice.model.Passenger;
import com.saiteja.bookingservice.model.enums.BookingStatus;
import com.saiteja.bookingservice.model.enums.TicketStatus;
import com.saiteja.bookingservice.repository.BookingRepository;
import com.saiteja.bookingservice.repository.TicketRepository;
import com.saiteja.bookingservice.service.BookingService;
import com.saiteja.bookingservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final FlightServiceClient flightServiceClient;

    @Override
    public Mono<TicketResponse> createBooking(BookingCreateRequest request) {

        if (request.getScheduleIds() == null || request.getScheduleIds().isEmpty()) {
            return Mono.error(new BadRequestException("At least one schedule id is required"));
        }

        return validateAndLockSeats(request)
                .then(Mono.defer(() -> {
                    String pnr = generatePNR();

                    Booking booking = Booking.builder()
                            .pnr(pnr)
                            .contactEmail(request.getContactEmail())
                            .scheduleIds(request.getScheduleIds())
                            .passengers(mapPassengers(request))
                            .status(BookingStatus.CONFIRMED)
                            .build();

                    return bookingRepository.save(booking)
                            .flatMap(savedBooking -> ticketService.generateTicket(savedBooking.getId()));
                }));
    }

    private Mono<Void> validateAndLockSeats(BookingCreateRequest request) {
        String scheduleId = request.getScheduleIds().get(0);
        List<String> seatNumbers = request.getPassengers().stream()
                .map(p -> p.getSeatNumber())
                .toList();

        if (seatNumbers.isEmpty()) {
            return Mono.error(new BadRequestException("At least one passenger is required"));
        }

        return flightServiceClient.lockSeats(scheduleId, seatNumbers);
    }

    @Override
    public Mono<BookingResponse> getBookingByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Booking not found")))
                .map(this::toResponse);
    }

    @Override
    public Flux<BookingResponse> getBookingsByEmail(String email) {
        return bookingRepository.findByContactEmail(email)
                .map(this::toResponse);
    }

    @Override
    public Mono<String> cancelBooking(String pnr) {

        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Booking not found")))
                .flatMap(booking -> {

                    if (booking.getStatus() == BookingStatus.CANCELLED)
                        return Mono.error(new BadRequestException("Booking already cancelled"));

                    booking.setStatus(BookingStatus.CANCELLED);

                    List<String> seatNumbers = booking.getPassengers().stream()
                            .map(Passenger::getSeatNumber)
                            .toList();

                    Mono<Void> restoreSeats = flightServiceClient.releaseSeats(booking.getScheduleIds().get(0), seatNumbers);

                    Mono<Void> cancelTicket = ticketRepository.findByPnr(pnr)
                            .flatMap(ticket -> {
                                ticket.setStatus(TicketStatus.CANCELLED);
                                return ticketRepository.save(ticket).then();
                            })
                            .onErrorResume(e -> Mono.empty());

                    return restoreSeats
                            .then(cancelTicket)
                            .then(bookingRepository.save(booking))
                            .thenReturn("Booking and Ticket Cancelled");
                });
    }

    private List<Passenger> mapPassengers(BookingCreateRequest request) {
        return request.getPassengers().stream()
                .map(p -> Passenger.builder()
                        .fullName(p.getFullName())
                        .gender(p.getGender())
                        .age(p.getAge())
                        .mealOption(p.getMealOption())
                        .seatNumber(p.getSeatNumber())
                        .build())
                .toList();
    }

    private BookingResponse toResponse(Booking booking) {
        List<PassengerResponse> passengers = booking.getPassengers().stream()
                .map(p -> PassengerResponse.builder()
                        .fullName(p.getFullName())
                        .gender(p.getGender())
                        .age(p.getAge())
                        .seatNumber(p.getSeatNumber())
                        .mealOption(p.getMealOption())
                        .build())
                .toList();

        return BookingResponse.builder()
                .pnr(booking.getPnr())
                .contactEmail(booking.getContactEmail())
                .scheduleIds(booking.getScheduleIds())
                .passengers(passengers)
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private String generatePNR() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}


