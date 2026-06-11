package com.marcus.eventhub.registration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    Optional<EventRegistration> findByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventIdAndStatus(UUID eventId, RegistrationStatus status);

    @EntityGraph(attributePaths = "user")
    List<EventRegistration> findByEventIdAndStatusOrderByRegisteredAtAsc(UUID eventId, RegistrationStatus status);
}
