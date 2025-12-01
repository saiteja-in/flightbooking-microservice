package com.saiteja.bookingservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FlightServiceClient {

    private final WebClient.Builder webClientBuilder;

    private WebClient client() {
        return webClientBuilder.baseUrl("http://flight-service").build();
    }

    public Mono<Void> lockSeats(String scheduleId, List<String> seatNumbers) {
        return client().post()
                .uri("/api/v1.0/flight/admin/internal/schedules/{id}/lock-seats", scheduleId)
                .body(BodyInserters.fromValue(seatNumbers))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> releaseSeats(String scheduleId, List<String> seatNumbers) {
        return client().post()
                .uri("/api/v1.0/flight/admin/internal/schedules/{id}/release-seats", scheduleId)
                .body(BodyInserters.fromValue(seatNumbers))
                .retrieve()
                .bodyToMono(Void.class);
    }
}


