package com.marcus.eventhub.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalEvents,
        long totalRegistrations
) {
}
