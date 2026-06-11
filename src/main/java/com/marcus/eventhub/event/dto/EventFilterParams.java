package com.marcus.eventhub.event.dto;

import java.time.Instant;

public record EventFilterParams(
        String title,
        String location,
        Instant startFrom,
        Instant startTo
) {
}
