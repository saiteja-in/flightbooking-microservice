package com.saiteja.bookingservice.controller;

import com.saiteja.bookingservice.dto.auth.AuthResponse;
import com.saiteja.bookingservice.dto.auth.UserLoginRequest;
import com.saiteja.bookingservice.dto.auth.UserRegisterRequest;
import com.saiteja.bookingservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTests {

    private WebTestClient webTestClient;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(authController).build();
    }

    @Test
    void register_shouldReturnCreated_whenValidRequest() {
        // Given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .message("Registration successful")
                .email("test@example.com")
                .role("USER")
                .build();

        when(authService.register(any(UserRegisterRequest.class)))
                .thenReturn(Mono.just(response));

        // When & Then
        webTestClient.post()
                .uri("/api/v1.0/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void login_shouldReturnOk_whenValidCredentials() {
        // Given
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .message("Login successful")
                .email("test@example.com")
                .role("USER")
                .build();

        when(authService.login(any(UserLoginRequest.class)))
                .thenReturn(Mono.just(response));

        // When & Then
        webTestClient.post()
                .uri("/api/v1.0/auth/login")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
}

