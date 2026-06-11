package com.marcus.eventhub.event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = "owner")
    @Query("SELECT e FROM Event e ORDER BY e.startDateTime ASC")
    List<Event> findAllWithOwner();

    @EntityGraph(attributePaths = "owner")
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithOwner(@Param("id") UUID id);

    @EntityGraph(attributePaths = "owner")
    List<Event> findByOwnerIdOrderByStartDateTimeAsc(UUID ownerId);

    @EntityGraph(attributePaths = "owner")
    @Query("""
            SELECT e FROM Event e
            WHERE e.startDateTime >= :weekStart
              AND e.startDateTime < :weekEnd
            ORDER BY e.startDateTime ASC
            """)
    List<Event> findEventsStartingThisWeek(
            @Param("weekStart") Instant weekStart,
            @Param("weekEnd") Instant weekEnd
    );

    @EntityGraph(attributePaths = "owner")
    @Query("""
            SELECT e FROM Event e
            JOIN EventRegistration r ON r.event = e
            WHERE r.user.id = :userId
              AND r.status = com.marcus.eventhub.registration.RegistrationStatus.CONFIRMED
            ORDER BY e.startDateTime ASC
            """)
    List<Event> findRegisteredEventsByUserId(@Param("userId") UUID userId);
}
