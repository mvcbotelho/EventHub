package com.marcus.eventhub.event.dto;

import com.marcus.eventhub.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "EventPageResponse", description = "Paginated list of events")
public record EventPageResponse(
        @Schema(description = "Events on the current page")
        List<EventResponse> content,
        @Schema(description = "Zero-based page index", example = "0")
        int page,
        @Schema(description = "Number of items per page", example = "20")
        int size,
        @Schema(description = "Total number of matching events", example = "42")
        long totalElements,
        @Schema(description = "Total number of pages", example = "3")
        int totalPages,
        @Schema(description = "Whether this is the last page")
        boolean last
) {

    public static EventPageResponse from(PageResponse<EventResponse> page) {
        return new EventPageResponse(
                page.content(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.last()
        );
    }
}
