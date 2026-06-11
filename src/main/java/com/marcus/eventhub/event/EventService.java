package com.marcus.eventhub.event;

import com.marcus.eventhub.auth.CurrentUserService;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ForbiddenException;
import com.marcus.eventhub.common.exception.ResourceNotFoundException;
import com.marcus.eventhub.event.dto.CreateEventRequest;
import com.marcus.eventhub.event.dto.EventResponse;
import com.marcus.eventhub.event.dto.UpdateEventRequest;
import com.marcus.eventhub.user.User;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final CurrentUserService currentUserService;

    public EventService(EventRepository eventRepository, CurrentUserService currentUserService) {
        this.eventRepository = eventRepository;
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

    public List<EventResponse> findAll() {
        return eventRepository.findAllWithOwner().stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse findById(UUID id) {
        return EventResponse.from(getEventOrThrow(id));
    }

    public List<EventResponse> findEventsThisWeek() {
        Instant weekStart = Instant.now()
                .atZone(ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant weekEnd = weekStart
                .atZone(ZoneOffset.UTC)
                .plusDays(7)
                .toInstant();

        return eventRepository.findEventsStartingThisWeek(weekStart, weekEnd).stream()
                .map(EventResponse::from)
                .toList();
    }

    public List<EventResponse> findMine() {
        User currentUser = currentUserService.getCurrentUser();
        return eventRepository.findByOwnerIdOrderByStartDateTimeAsc(currentUser.getId()).stream()
                .map(EventResponse::from)
                .toList();
    }

    public List<EventResponse> findRegistered() {
        User currentUser = currentUserService.getCurrentUser();
        return eventRepository.findRegisteredEventsByUserId(currentUser.getId()).stream()
                .map(EventResponse::from)
                .toList();
    }

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest request) {
        validateDateRange(request.startDateTime(), request.endDateTime());

        Event event = getEventOrThrow(id);
        assertOwner(event);

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
        eventRepository.delete(event);
    }

    public Event getEventOrThrow(UUID id) {
        return eventRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private void assertOwner(Event event) {
        User currentUser = currentUserService.getCurrentUser();
        if (!event.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the event owner can perform this action");
        }
    }

    private void validateDateRange(Instant startDateTime, Instant endDateTime) {
        if (endDateTime.isBefore(startDateTime)) {
            throw new BusinessException("End date and time cannot be before start date and time");
        }
    }
}
