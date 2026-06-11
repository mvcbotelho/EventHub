package com.marcus.eventhub.registration.dto;

import com.marcus.eventhub.registration.EventWaitlistEntry;
import java.time.Instant;
import java.util.UUID;

public record WaitlistEntryResponse(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        Instant joinedAt
) {

    public static WaitlistEntryResponse from(EventWaitlistEntry entry) {
        return new WaitlistEntryResponse(
                entry.getId(),
                entry.getUser().getId(),
                entry.getUser().getName(),
                entry.getUser().getEmail(),
                entry.getJoinedAt()
        );
    }
}
