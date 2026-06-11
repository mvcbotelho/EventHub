package com.marcus.eventhub.event.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;

public record CreateEventRequest(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Start date and time are required")
        @FutureOrPresent(message = "Start date and time cannot be in the past")
        Instant startDateTime,

        @NotNull(message = "End date and time are required")
        Instant endDateTime,

        @NotNull(message = "Maximum number of participants is required")
        @Positive(message = "Maximum number of participants must be positive")
        Integer maxParticipants,

        UUID categoryId
) {
}
