package com.marcus.eventhub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication tokens returned on login and refresh")
public record AuthResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,
        @Schema(description = "Opaque refresh token used to obtain new access tokens")
        String refreshToken,
        @Schema(description = "Token type", example = "Bearer")
        String type
) {
    public static AuthResponse bearer(String token, String refreshToken) {
        return new AuthResponse(token, refreshToken, "Bearer");
    }
}
