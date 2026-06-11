package com.marcus.eventhub.event;

import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ResourceNotFoundException;
import com.marcus.eventhub.event.dto.CreateEventRequest;
import com.marcus.eventhub.event.dto.EventResponse;
import com.marcus.eventhub.event.dto.UpdateEventRequest;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        validateDateRange(request.startDateTime(), request.endDateTime());

        Event event = new Event(
                request.title(),
                request.description(),
                request.location(),
                request.startDateTime(),
                request.endDateTime(),
                request.maxParticipants()
        );

        return EventResponse.from(eventRepository.save(event));
    }

    public List<EventResponse> findAll() {
        return eventRepository.findAll().stream()
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

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest request) {
        validateDateRange(request.startDateTime(), request.endDateTime());

        Event event = getEventOrThrow(id);
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
        eventRepository.delete(event);
    }

    private Event getEventOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
    }

    private void validateDateRange(Instant startDateTime, Instant endDateTime) {
        if (endDateTime.isBefore(startDateTime)) {
            throw new BusinessException("A data de fim não pode ser anterior à data de início");
        }
    }
}
