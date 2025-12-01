package com.saiteja.flightservice.model;

import com.saiteja.flightservice.model.enums.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flight_schedules")
public class FlightSchedule {

    @Id
    private String id;

    private String flightId;

    private LocalDate flightDate;

    private LocalTime departureTime;

    private LocalTime arrivalTime;

    private BigDecimal fare;

    private Integer totalSeats;

    private Integer availableSeats;

    private FlightStatus status;

    private List<String> bookedSeats;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}


