package com.marcus.eventhub.event;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    @EntityGraph(attributePaths = "owner")
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithOwner(@Param("id") UUID id);

    @EntityGraph(attributePaths = "owner")
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}
