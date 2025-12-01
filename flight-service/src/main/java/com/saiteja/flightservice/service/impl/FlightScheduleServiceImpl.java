package com.saiteja.flightservice.service.impl;

import com.saiteja.flightservice.dto.flight.FlightScheduleCreateRequest;
import com.saiteja.flightservice.dto.flight.FlightScheduleResponse;
import com.saiteja.flightservice.exception.ResourceNotFoundException;
import com.saiteja.flightservice.model.Flight;
import com.saiteja.flightservice.model.FlightSchedule;
import com.saiteja.flightservice.model.enums.FlightStatus;
import com.saiteja.flightservice.repository.FlightRepository;
import com.saiteja.flightservice.repository.FlightScheduleRepository;
import com.saiteja.flightservice.service.FlightScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FlightScheduleServiceImpl implements FlightScheduleService {

    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightRepository flightRepository;

    @Override
    public Flux<FlightScheduleResponse> searchFlights(String origin, String destination, LocalDate date) {

        return flightRepository.findByOriginAirportAndDestinationAirport(origin, destination)
                .flatMap(flight ->
                        flightScheduleRepository.findByFlightIdAndFlightDate(flight.getId(), date)
                                .map(schedule -> toResponse(schedule, flight))
                )
                .switchIfEmpty(Flux.error(new ResourceNotFoundException("No schedule found for given criteria")));
    }

    @Override
    public Mono<FlightScheduleResponse> createSchedule(FlightScheduleCreateRequest request) {

        return flightRepository.findByFlightNumber(request.getFlightNumber().trim().toUpperCase())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Flight not found with number: " + request.getFlightNumber()
                )))
                .flatMap(flight -> {

                    FlightSchedule schedule = FlightSchedule.builder()
                            .flightId(flight.getId())
                            .flightDate(request.getFlightDate())
                            .departureTime(request.getDepartureTime())
                            .arrivalTime(request.getArrivalTime())
                            .fare(request.getFare())
                            .totalSeats(flight.getSeatCapacity())
                            .availableSeats(flight.getSeatCapacity())
                            .status(FlightStatus.SCHEDULED)
                            .build();

                    return flightScheduleRepository.save(schedule)
                            .map(saved -> toResponse(saved, flight));
                });
    }

    @Override
    public Mono<FlightScheduleResponse> getScheduleById(String id) {
        return flightScheduleRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Flight schedule not found: " + id)))
                .flatMap(schedule ->
                        flightRepository.findById(schedule.getFlightId())
                                .map(flight -> toResponse(schedule, flight))
                );
    }

    @Override
    public Mono<Void> lockSeats(String scheduleId, java.util.List<String> seatNumbers) {
        int seatsToBook = seatNumbers.size();
        return flightScheduleRepository.findById(scheduleId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Flight schedule not found: " + scheduleId)))
                .flatMap(schedule -> {
                    if (schedule.getAvailableSeats() < seatsToBook) {
                        return Mono.error(new com.saiteja.flightservice.exception.BadRequestException("Not enough seats available"));
                    }
                    java.util.List<String> booked = schedule.getBookedSeats();
                    if (booked == null) {
                        booked = new java.util.ArrayList<>();
                        schedule.setBookedSeats(booked);
                    }
                    booked.addAll(seatNumbers);
                    schedule.setAvailableSeats(schedule.getAvailableSeats() - seatsToBook);
                    return flightScheduleRepository.save(schedule).then();
                });
    }

    @Override
    public Mono<Void> releaseSeats(String scheduleId, java.util.List<String> seatNumbers) {
        int seatsToRelease = seatNumbers.size();
        return flightScheduleRepository.findById(scheduleId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Flight schedule not found: " + scheduleId)))
                .flatMap(schedule -> {
                    if (schedule.getBookedSeats() != null) {
                        schedule.getBookedSeats().removeIf(seatNumbers::contains);
                    }
                    schedule.setAvailableSeats(schedule.getAvailableSeats() + seatsToRelease);
                    return flightScheduleRepository.save(schedule).then();
                });
    }

    private FlightScheduleResponse toResponse(FlightSchedule schedule, Flight flight) {
        return FlightScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline().name())
                .originAirport(flight.getOriginAirport())
                .destinationAirport(flight.getDestinationAirport())
                .flightDate(schedule.getFlightDate())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .fare(schedule.getFare())
                .availableSeats(schedule.getAvailableSeats())
                .build();
    }
}


