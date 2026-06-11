package com.marcus.eventhub.registration.dto;

import com.marcus.eventhub.registration.EventRegistration;
import com.marcus.eventhub.registration.RegistrationStatus;
import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        UUID eventId,
        UUID userId,
        RegistrationStatus status,
        Instant registeredAt
) {

    public static RegistrationResponse from(EventRegistration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getUser().getId(),
                registration.getStatus(),
                registration.getRegisteredAt()
        );
    }
}
