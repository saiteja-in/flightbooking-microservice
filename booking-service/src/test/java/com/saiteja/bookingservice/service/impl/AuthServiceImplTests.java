package com.saiteja.bookingservice.service.impl;

import com.saiteja.bookingservice.dto.auth.AuthResponse;
import com.saiteja.bookingservice.dto.auth.UserLoginRequest;
import com.saiteja.bookingservice.dto.auth.UserRegisterRequest;
import com.saiteja.bookingservice.exception.BadRequestException;
import com.saiteja.bookingservice.exception.DuplicateResourceException;
import com.saiteja.bookingservice.exception.ResourceNotFoundException;
import com.saiteja.bookingservice.model.User;
import com.saiteja.bookingservice.model.enums.UserRole;
import com.saiteja.bookingservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private BCryptPasswordEncoder encoder;
    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();

        registerRequest = new UserRegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");
        testUser.setPassword(encoder.encode("password123"));
        testUser.setRole(UserRole.USER);
    }

    @Test
    void register_shouldReturnAuthResponse_whenEmailNotExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        // When
        Mono<AuthResponse> result = authService.register(registerRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getEmail()).isEqualTo("test@example.com");
                    assertThat(response.getRole()).isEqualTo("USER");
                    assertThat(response.getMessage()).isEqualTo("Registration successful");
                })
                .verifyComplete();
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenEmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        // When
        Mono<AuthResponse> result = authService.register(registerRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof DuplicateResourceException
                        && throwable.getMessage().equals("Email already registered"))
                .verify();
    }

    @Test
    void register_shouldTrimEmail() {
        // Given
        registerRequest.setEmail("  test@example.com  ");
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user123");
            return Mono.just(user);
        });

        // When
        Mono<AuthResponse> result = authService.register(registerRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getEmail()).isEqualTo("test@example.com");
                })
                .verifyComplete();
    }

    @Test
    void register_shouldEncodePassword() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getPassword()).isNotEqualTo("password123");
            assertThat(encoder.matches("password123", user.getPassword())).isTrue();
            user.setId("user123");
            return Mono.just(user);
        });

        // When
        Mono<AuthResponse> result = authService.register(registerRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));

        // When
        Mono<AuthResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getEmail()).isEqualTo("test@example.com");
                    assertThat(response.getRole()).isEqualTo("USER");
                    assertThat(response.getMessage()).isEqualTo("Login successful");
                })
                .verifyComplete();
    }

    @Test
    void login_shouldThrowResourceNotFoundException_whenUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        // When
        Mono<AuthResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("User not found"))
                .verify();
    }

    @Test
    void login_shouldThrowBadRequestException_whenPasswordIsInvalid() {
        // Given
        loginRequest.setPassword("wrongpassword");
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));

        // When
        Mono<AuthResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().equals("Invalid credentials"))
                .verify();
    }
}

