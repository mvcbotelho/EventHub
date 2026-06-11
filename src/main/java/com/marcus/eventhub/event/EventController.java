package com.marcus.eventhub.event;

import com.marcus.eventhub.event.dto.CreateEventRequest;
import com.marcus.eventhub.event.dto.EventFilterParams;
import com.marcus.eventhub.event.dto.EventPageResponse;
import com.marcus.eventhub.event.dto.EventResponse;
import com.marcus.eventhub.event.dto.UpdateEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Event CRUD, paginated listings, and soft delete")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Create an event", description = "The authenticated user becomes the event owner.")
    @ApiResponse(responseCode = "201", description = "Event created")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List all events",
            description = "Returns a paginated list. Supports optional filters and Spring Data pagination (`page`, `size`, `sort`)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated events",
            content = @Content(schema = @Schema(implementation = EventPageResponse.class))
    )
    public EventPageResponse findAll(
            @ParameterObject EventFilterParams filter,
            @ParameterObject @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return EventPageResponse.from(eventService.findAll(filter, pageable));
    }

    @GetMapping("/mine")
    @Operation(summary = "List events created by the authenticated user")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = EventPageResponse.class))
    )
    public EventPageResponse findMine(
            @ParameterObject EventFilterParams filter,
            @ParameterObject @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return EventPageResponse.from(eventService.findMine(filter, pageable));
    }

    @GetMapping("/this-week")
    @Operation(
            summary = "List events starting this week",
            description = "Week starts Monday 00:00 UTC. Same pagination and filters as `GET /events`."
    )
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = EventPageResponse.class))
    )
    public EventPageResponse findEventsThisWeek(
            @ParameterObject EventFilterParams filter,
            @ParameterObject @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return EventPageResponse.from(eventService.findEventsThisWeek(filter, pageable));
    }

    @GetMapping("/registered")
    @Operation(summary = "List events the authenticated user is registered for")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = EventPageResponse.class))
    )
    public EventPageResponse findRegistered(
            @ParameterObject EventFilterParams filter,
            @ParameterObject @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return EventPageResponse.from(eventService.findRegistered(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    @ApiResponse(responseCode = "200", description = "Event details")
    @ApiResponse(responseCode = "404", description = "Event not found or soft-deleted")
    public EventResponse findById(@PathVariable UUID id) {
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an event",
            description = "Owner only. Blocked when the event has confirmed registrations."
    )
    @ApiResponse(responseCode = "403", description = "Not the event owner")
    @ApiResponse(responseCode = "400", description = "Event has confirmed registrations")
    public EventResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft-delete an event",
            description = "Owner only. The event is hidden from listings and returns 404 on direct lookup."
    )
    @ApiResponse(responseCode = "204", description = "Event soft-deleted")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
