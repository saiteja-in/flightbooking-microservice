package com.saiteja.flightservice.controller;

import com.saiteja.flightservice.dto.flight.FlightScheduleCreateRequest;
import com.saiteja.flightservice.dto.flight.FlightScheduleResponse;
import com.saiteja.flightservice.dto.flight.FlightSearchRequest;
import com.saiteja.flightservice.service.FlightScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1.0/flight/admin")
@RequiredArgsConstructor
@Validated
public class FlightScheduleController {

    private final FlightScheduleService flightScheduleService;

    @PostMapping("/inventory")
    public Mono<ResponseEntity<FlightScheduleResponse>> addInventory(@Valid @RequestBody FlightScheduleCreateRequest request) {
        return flightScheduleService.createSchedule(request)
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @PostMapping("/search")
    public Mono<ResponseEntity<Flux<FlightScheduleResponse>>> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        return Mono.just(
                ResponseEntity.ok(
                        flightScheduleService.searchFlights(
                                request.getOriginAirport().trim().toUpperCase(),
                                request.getDestinationAirport().trim().toUpperCase(),
                                request.getFlightDate()
                        )
                )
        );
    }

    // Internal endpoint for booking-service to fetch schedule by id
    @GetMapping("/internal/schedules/{id}")
    public Mono<ResponseEntity<FlightScheduleResponse>> getScheduleById(@PathVariable String id) {
        return flightScheduleService.getScheduleById(id)
                .map(ResponseEntity::ok);
    }

    // Internal endpoint for booking-service to lock seats
    @PostMapping("/internal/schedules/{id}/lock-seats")
    public Mono<ResponseEntity<Void>> lockSeats(@PathVariable String id, @RequestBody java.util.List<String> seatNumbers) {
        return flightScheduleService.lockSeats(id, seatNumbers)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    // Internal endpoint for booking-service to release seats
    @PostMapping("/internal/schedules/{id}/release-seats")
    public Mono<ResponseEntity<Void>> releaseSeats(@PathVariable String id, @RequestBody java.util.List<String> seatNumbers) {
        return flightScheduleService.releaseSeats(id, seatNumbers)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
}


