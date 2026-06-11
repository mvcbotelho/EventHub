package com.marcus.eventhub.registration.dto;

import com.marcus.eventhub.registration.EventRegistration;
import java.time.Instant;
import java.util.UUID;

public record ParticipantResponse(
        UUID userId,
        String userName,
        String userEmail,
        Instant registeredAt
) {

    public static ParticipantResponse from(EventRegistration registration) {
        return new ParticipantResponse(
                registration.getUser().getId(),
                registration.getUser().getName(),
                registration.getUser().getEmail(),
                registration.getRegisteredAt()
        );
    }
}
