package com.marcus.eventhub.registration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventWaitlistRepository extends JpaRepository<EventWaitlistEntry, UUID> {

    Optional<EventWaitlistEntry> findByEventIdAndUserId(UUID eventId, UUID userId);

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    @EntityGraph(attributePaths = "user")
    List<EventWaitlistEntry> findByEventIdOrderByJoinedAtAsc(UUID eventId);

    Optional<EventWaitlistEntry> findFirstByEventIdOrderByJoinedAtAsc(UUID eventId);
}
