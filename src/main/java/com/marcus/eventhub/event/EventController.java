package com.marcus.eventhub.event;

import com.marcus.eventhub.event.dto.CreateEventRequest;
import com.marcus.eventhub.event.dto.EventResponse;
import com.marcus.eventhub.event.dto.UpdateEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@Tag(name = "Events", description = "Operações de eventos")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Criar evento")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os eventos")
    public List<EventResponse> findAll() {
        return eventService.findAll();
    }

    @GetMapping("/this-week")
    @Operation(summary = "Listar eventos da semana atual")
    public List<EventResponse> findEventsThisWeek() {
        return eventService.findEventsThisWeek();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    public EventResponse findById(@PathVariable UUID id) {
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento")
    public EventResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir evento")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
