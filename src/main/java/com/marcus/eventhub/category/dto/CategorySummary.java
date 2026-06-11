package com.marcus.eventhub.category.dto;

import com.marcus.eventhub.category.Category;
import java.util.UUID;

public record CategorySummary(
        UUID id,
        String name,
        String slug
) {

    public static CategorySummary from(Category category) {
        if (category == null) {
            return null;
        }
        return new CategorySummary(category.getId(), category.getName(), category.getSlug());
    }
}
