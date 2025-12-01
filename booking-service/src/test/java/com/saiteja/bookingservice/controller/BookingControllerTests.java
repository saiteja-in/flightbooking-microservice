package com.saiteja.bookingservice.controller;

import com.saiteja.bookingservice.dto.booking.BookingCreateRequest;
import com.saiteja.bookingservice.dto.passenger.PassengerRequest;
import com.saiteja.bookingservice.model.enums.Gender;
import com.saiteja.bookingservice.model.enums.MealOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void bookFlight_returnsCreated() {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setContactEmail("test@example.com");
        PassengerRequest p = new PassengerRequest();
        p.setFullName("John Doe");
        p.setGender(Gender.MALE);
        p.setAge(30);
        p.setSeatNumber("1A");
        p.setMealOption(MealOption.VEG);
        req.setPassengers(List.of(p));

        webTestClient.post()
                .uri("/api/v1.0/flight/booking/abc123")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();
    }
}


