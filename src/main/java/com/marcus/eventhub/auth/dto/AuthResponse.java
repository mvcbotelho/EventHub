package com.marcus.eventhub.auth.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        String type
) {
    public static AuthResponse bearer(String token, String refreshToken) {
        return new AuthResponse(token, refreshToken, "Bearer");
    }
}
