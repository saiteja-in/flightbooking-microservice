package com.saiteja.bookingservice.service.impl;

import com.saiteja.bookingservice.dto.passenger.PassengerResponse;
import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import com.saiteja.bookingservice.exception.ResourceNotFoundException;
import com.saiteja.bookingservice.model.Ticket;
import com.saiteja.bookingservice.model.enums.TicketStatus;
import com.saiteja.bookingservice.repository.BookingRepository;
import com.saiteja.bookingservice.repository.TicketRepository;
import com.saiteja.bookingservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Mono<TicketResponse> generateTicket(String bookingId) {

        return bookingRepository.findById(bookingId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Booking not found")))
                .flatMap(booking -> {

                    Ticket ticket = Ticket.builder()
                            .pnr(booking.getPnr())
                            .bookingId(booking.getId())
                            .scheduleId(booking.getScheduleIds().get(0))
                            .passengers(booking.getPassengers())
                            .issuedAt(LocalDateTime.now())
                            .build();

                    return ticketRepository.save(ticket)
                            .map(this::toResponse);
                });
    }

    @Override
    public Mono<TicketResponse> getTicketByPnr(String pnr) {
        return ticketRepository.findByPnr(pnr)
                .filter(ticket -> ticket.getStatus() != TicketStatus.CANCELLED)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ticket not found")))
                .map(this::toResponse);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .pnr(ticket.getPnr())
                .bookingId(ticket.getBookingId())
                .passengers(
                        ticket.getPassengers()
                                .stream()
                                .map(p -> PassengerResponse.builder()
                                        .fullName(p.getFullName())
                                        .gender(p.getGender())
                                        .age(p.getAge())
                                        .seatNumber(p.getSeatNumber())
                                        .mealOption(p.getMealOption())
                                        .build())
                                .toList()
                )
                .issuedAt(ticket.getIssuedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}


