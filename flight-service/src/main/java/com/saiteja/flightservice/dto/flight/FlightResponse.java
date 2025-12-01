package com.saiteja.flightservice.dto.flight;

import com.saiteja.flightservice.model.enums.Airline;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlightResponse {
    private String id;
    private String flightNumber;
    private Airline airline;
    private String originAirport;
    private String destinationAirport;
    private Integer seatCapacity;
}


