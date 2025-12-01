package com.saiteja.bookingservice.service;

import com.saiteja.bookingservice.dto.booking.BookingCreateRequest;
import com.saiteja.bookingservice.dto.booking.BookingResponse;
import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {

    Mono<TicketResponse> createBooking(BookingCreateRequest request);

    Mono<BookingResponse> getBookingByPnr(String pnr);

    Flux<BookingResponse> getBookingsByEmail(String email);

    Mono<String> cancelBooking(String pnr);
}


