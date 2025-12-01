package com.saiteja.flightservice.service;

import com.saiteja.flightservice.dto.flight.FlightScheduleCreateRequest;
import com.saiteja.flightservice.dto.flight.FlightScheduleResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface FlightScheduleService {

    Flux<FlightScheduleResponse> searchFlights(String origin, String destination, LocalDate date);

    Mono<FlightScheduleResponse> createSchedule(FlightScheduleCreateRequest request);

    Mono<FlightScheduleResponse> getScheduleById(String id);

    Mono<Void> lockSeats(String scheduleId, java.util.List<String> seatNumbers);

    Mono<Void> releaseSeats(String scheduleId, java.util.List<String> seatNumbers);
}


