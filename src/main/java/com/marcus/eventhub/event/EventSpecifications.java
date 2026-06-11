package com.marcus.eventhub.event;

import com.marcus.eventhub.event.dto.EventFilterParams;
import com.marcus.eventhub.registration.EventRegistration;
import com.marcus.eventhub.registration.RegistrationStatus;
import jakarta.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> withFilters(EventFilterParams filter) {
        Specification<Event> spec = Specification.where(null);

        if (filter.title() != null && !filter.title().isBlank()) {
            String pattern = "%" + filter.title().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), pattern));
        }

        if (filter.location() != null && !filter.location().isBlank()) {
            String pattern = "%" + filter.location().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("location")), pattern));
        }

        if (filter.startFrom() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("startDateTime"), filter.startFrom()));
        }

        if (filter.startTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("startDateTime"), filter.startTo()));
        }

        if (filter.categorySlug() != null && !filter.categorySlug().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.join("category").get("slug")), filter.categorySlug().toLowerCase()));
        }

        return spec;
    }

    public static Specification<Event> ownedBy(UUID ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Event> startingBetween(Instant start, Instant end) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("startDateTime"), start),
                cb.lessThan(root.get("startDateTime"), end)
        );
    }

    public static Specification<Event> registeredBy(UUID userId) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            var registration = subquery.from(EventRegistration.class);
            subquery.select(cb.literal(1L));
            subquery.where(
                    cb.equal(registration.get("event"), root),
                    cb.equal(registration.get("user").get("id"), userId),
                    cb.equal(registration.get("status"), RegistrationStatus.CONFIRMED)
            );
            return cb.exists(subquery);
        };
    }
}
