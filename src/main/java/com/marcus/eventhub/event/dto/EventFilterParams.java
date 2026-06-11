package com.marcus.eventhub.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "EventFilterParams", description = "Optional filters for event listings")
public record EventFilterParams(
        @Schema(description = "Case-insensitive partial match on event title", example = "Meetup")
        String title,
        @Schema(description = "Case-insensitive partial match on location", example = "New York")
        String location,
        @Schema(description = "Minimum start date/time (inclusive, UTC)", example = "2026-12-01T00:00:00Z")
        Instant startFrom,
        @Schema(description = "Maximum start date/time (inclusive, UTC)", example = "2026-12-31T23:59:59Z")
        Instant startTo
) {
}
