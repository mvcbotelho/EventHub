package com.marcus.eventhub.event.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record CreateEventRequest(
        @NotBlank(message = "O título é obrigatório")
        String title,

        String description,

        @NotBlank(message = "O local é obrigatório")
        String location,

        @NotNull(message = "A data de início é obrigatória")
        @FutureOrPresent(message = "A data de início não pode estar no passado")
        Instant startDateTime,

        @NotNull(message = "A data de fim é obrigatória")
        Instant endDateTime,

        @NotNull(message = "O número máximo de participantes é obrigatório")
        @Positive(message = "O número máximo de participantes deve ser positivo")
        Integer maxParticipants
) {
}
