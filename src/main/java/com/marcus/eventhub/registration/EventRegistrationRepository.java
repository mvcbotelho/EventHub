package com.marcus.eventhub.registration;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    Optional<EventRegistration> findByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventIdAndStatus(UUID eventId, RegistrationStatus status);

    @EntityGraph(attributePaths = "user")
    List<EventRegistration> findByEventIdAndStatusOrderByRegisteredAtAsc(UUID eventId, RegistrationStatus status);

    @Query("""
            SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
            FROM EventRegistration r
            JOIN r.event e
            WHERE r.user.id = :userId
              AND r.status = com.marcus.eventhub.registration.RegistrationStatus.CONFIRMED
              AND e.id <> :excludeEventId
              AND e.startDateTime < :endDateTime
              AND e.endDateTime > :startDateTime
            """)
    boolean existsOverlappingConfirmedRegistration(
            @Param("userId") UUID userId,
            @Param("excludeEventId") UUID excludeEventId,
            @Param("startDateTime") Instant startDateTime,
            @Param("endDateTime") Instant endDateTime
    );
}
