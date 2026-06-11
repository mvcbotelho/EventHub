package com.marcus.eventhub.event;

import com.marcus.eventhub.auth.CurrentUserService;
import com.marcus.eventhub.common.dto.PageResponse;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ForbiddenException;
import com.marcus.eventhub.common.exception.ResourceNotFoundException;
import com.marcus.eventhub.event.dto.CreateEventRequest;
import com.marcus.eventhub.event.dto.EventFilterParams;
import com.marcus.eventhub.event.dto.EventResponse;
import com.marcus.eventhub.event.dto.UpdateEventRequest;
import com.marcus.eventhub.registration.EventRegistrationRepository;
import com.marcus.eventhub.registration.RegistrationStatus;
import com.marcus.eventhub.user.User;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final CurrentUserService currentUserService;

    public EventService(
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            CurrentUserService currentUserService
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        validateDateRange(request.startDateTime(), request.endDateTime());

        User owner = currentUserService.getCurrentUser();
        Event event = new Event(
                request.title(),
                request.description(),
                request.location(),
                request.startDateTime(),
                request.endDateTime(),
                request.maxParticipants(),
                owner
        );

        return EventResponse.from(eventRepository.save(event));
    }

    public PageResponse<EventResponse> findAll(EventFilterParams filter, Pageable pageable) {
        return mapPage(eventRepository.findAll(EventSpecifications.withFilters(filter), pageable));
    }

    public EventResponse findById(UUID id) {
        return EventResponse.from(getEventOrThrow(id));
    }

    public PageResponse<EventResponse> findEventsThisWeek(EventFilterParams filter, Pageable pageable) {
        Instant weekStart = Instant.now()
                .atZone(ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant weekEnd = weekStart.atZone(ZoneOffset.UTC).plusDays(7).toInstant();

        Specification<Event> spec = EventSpecifications.withFilters(filter)
                .and(EventSpecifications.startingBetween(weekStart, weekEnd));

        return mapPage(eventRepository.findAll(spec, pageable));
    }

    public PageResponse<EventResponse> findMine(EventFilterParams filter, Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();
        Specification<Event> spec = EventSpecifications.withFilters(filter)
                .and(EventSpecifications.ownedBy(currentUser.getId()));

        return mapPage(eventRepository.findAll(spec, pageable));
    }

    public PageResponse<EventResponse> findRegistered(EventFilterParams filter, Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();
        Specification<Event> spec = EventSpecifications.withFilters(filter)
                .and(EventSpecifications.registeredBy(currentUser.getId()));

        return mapPage(eventRepository.findAll(spec, pageable));
    }

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest request) {
        validateDateRange(request.startDateTime(), request.endDateTime());

        Event event = getEventOrThrow(id);
        assertOwner(event);
        assertNoConfirmedRegistrants(id);

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setStartDateTime(request.startDateTime());
        event.setEndDateTime(request.endDateTime());
        event.setMaxParticipants(request.maxParticipants());

        return EventResponse.from(event);
    }

    @Transactional
    public void delete(UUID id) {
        Event event = getEventOrThrow(id);
        assertOwner(event);
        event.softDelete();
        eventRepository.save(event);
    }

    public Event getEventOrThrow(UUID id) {
        return eventRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private PageResponse<EventResponse> mapPage(org.springframework.data.domain.Page<Event> page) {
        return PageResponse.from(page, EventResponse::from);
    }

    private void assertOwner(Event event) {
        User currentUser = currentUserService.getCurrentUser();
        if (!event.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the event owner can perform this action");
        }
    }

    private void assertNoConfirmedRegistrants(UUID eventId) {
        long confirmedCount = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
        if (confirmedCount > 0) {
            throw new BusinessException("Cannot update an event that has confirmed registrations");
        }
    }

    private void validateDateRange(Instant startDateTime, Instant endDateTime) {
        if (endDateTime.isBefore(startDateTime)) {
            throw new BusinessException("End date and time cannot be before start date and time");
        }
    }
}
