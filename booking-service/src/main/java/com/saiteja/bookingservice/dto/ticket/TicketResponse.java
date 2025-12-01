package com.saiteja.bookingservice.dto.ticket;

import com.saiteja.bookingservice.dto.passenger.PassengerResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TicketResponse {
    private String pnr;
    private String bookingId;
    private List<PassengerResponse> passengers;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
