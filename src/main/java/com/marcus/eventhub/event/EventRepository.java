package com.marcus.eventhub.event;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
            SELECT e FROM Event e
            WHERE e.startDateTime >= :weekStart
              AND e.startDateTime < :weekEnd
            ORDER BY e.startDateTime ASC
            """)
    java.util.List<Event> findEventsStartingThisWeek(
            @Param("weekStart") Instant weekStart,
            @Param("weekEnd") Instant weekEnd
    );
}
