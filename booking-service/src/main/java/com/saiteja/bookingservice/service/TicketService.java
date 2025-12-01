package com.saiteja.bookingservice.service;

import com.saiteja.bookingservice.dto.ticket.TicketResponse;
import reactor.core.publisher.Mono;

public interface TicketService {

    Mono<TicketResponse> generateTicket(String bookingId);

    Mono<TicketResponse> getTicketByPnr(String pnr);
}


