package com.saiteja.flightservice.repository;

import com.saiteja.flightservice.model.FlightSchedule;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface FlightScheduleRepository extends ReactiveMongoRepository<FlightSchedule, String> {

    Flux<FlightSchedule> findByFlightIdAndFlightDate(String flightId, LocalDate date);
}


