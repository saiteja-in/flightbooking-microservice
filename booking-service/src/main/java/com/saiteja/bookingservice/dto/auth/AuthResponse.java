package com.saiteja.bookingservice.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String message;
    private String email;
    private String role;
}


