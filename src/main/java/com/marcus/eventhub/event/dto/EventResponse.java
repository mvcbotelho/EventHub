package com.marcus.eventhub.event.dto;

import com.marcus.eventhub.category.Category;
import com.marcus.eventhub.category.dto.CategorySummary;
import com.marcus.eventhub.event.Event;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        String location,
        Instant startDateTime,
        Instant endDateTime,
        Integer maxParticipants,
        UUID ownerId,
        String ownerName,
        CategorySummary category,
        Instant createdAt,
        Instant updatedAt
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getMaxParticipants(),
                event.getOwner().getId(),
                event.getOwner().getName(),
                CategorySummary.from(event.getCategory()),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
