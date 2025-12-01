package com.saiteja.flightservice.controller;

import com.saiteja.flightservice.dto.flight.FlightCreateRequest;
import com.saiteja.flightservice.model.enums.Airline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlightControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void createFlight_validationError() {
        FlightCreateRequest req = new FlightCreateRequest();
        req.setAirline(Airline.AIR_INDIA);
        // missing required fields

        webTestClient.post()
                .uri("/api/v1.0/flight/admin/flights")
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }
}


