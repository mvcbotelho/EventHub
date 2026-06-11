package com.marcus.eventhub.category.dto;

import com.marcus.eventhub.category.Category;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        Instant createdAt
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getCreatedAt()
        );
    }
}
