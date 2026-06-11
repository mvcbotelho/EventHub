package com.marcus.eventhub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token payload")
public record RefreshTokenRequest(
        @Schema(
                description = "Refresh token returned by login or a previous refresh",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotBlank
        String refreshToken
) {
}
