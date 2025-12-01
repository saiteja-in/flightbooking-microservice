package com.saiteja.bookingservice.service;

import com.saiteja.bookingservice.dto.auth.AuthResponse;
import com.saiteja.bookingservice.dto.auth.UserLoginRequest;
import com.saiteja.bookingservice.dto.auth.UserRegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<AuthResponse> register(UserRegisterRequest request);

    Mono<AuthResponse> login(UserLoginRequest request);
}


