package com.marcus.eventhub.auth.dto;

public record AuthResponse(
        String token,
        String type
) {
    public static AuthResponse bearer(String token) {
        return new AuthResponse(token, "Bearer");
    }
}
