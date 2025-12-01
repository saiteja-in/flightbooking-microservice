package com.saiteja.bookingservice.repository;

import com.saiteja.bookingservice.model.Booking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String> {

    Mono<Booking> findByPnr(String pnr);

    Flux<Booking> findByContactEmail(String email);
}


