package com.saiteja.flightservice.repository;

import com.saiteja.flightservice.model.Flight;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightRepository extends ReactiveMongoRepository<Flight, String> {

    Flux<Flight> findByOriginAirportAndDestinationAirport(String origin, String destination);

    Mono<Flight> findByFlightNumber(String flightNumber);

    Mono<Boolean> existsByFlightNumber(String flightNumber);
}


