package com.marcus.eventhub.event;

import com.marcus.eventhub.common.dto.PageResponse;
import com.marcus.eventhub.event.dto.CreateEventRequest;
import com.marcus.eventhub.event.dto.EventFilterParams;
import com.marcus.eventhub.event.dto.EventResponse;
import com.marcus.eventhub.event.dto.UpdateEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Event operations")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Create an event")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all events (paginated, with optional filters)")
    public PageResponse<EventResponse> findAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Instant startFrom,
            @RequestParam(required = false) Instant startTo,
            @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return eventService.findAll(new EventFilterParams(title, location, startFrom, startTo), pageable);
    }

    @GetMapping("/mine")
    @Operation(summary = "List events created by the authenticated user")
    public PageResponse<EventResponse> findMine(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Instant startFrom,
            @RequestParam(required = false) Instant startTo,
            @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return eventService.findMine(new EventFilterParams(title, location, startFrom, startTo), pageable);
    }

    @GetMapping("/this-week")
    @Operation(summary = "List events starting this week")
    public PageResponse<EventResponse> findEventsThisWeek(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Instant startFrom,
            @RequestParam(required = false) Instant startTo,
            @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return eventService.findEventsThisWeek(new EventFilterParams(title, location, startFrom, startTo), pageable);
    }

    @GetMapping("/registered")
    @Operation(summary = "List events the authenticated user is registered for")
    public PageResponse<EventResponse> findRegistered(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Instant startFrom,
            @RequestParam(required = false) Instant startTo,
            @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return eventService.findRegistered(new EventFilterParams(title, location, startFrom, startTo), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    public EventResponse findById(@PathVariable UUID id) {
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event (owner only)")
    public EventResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an event (owner only)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
