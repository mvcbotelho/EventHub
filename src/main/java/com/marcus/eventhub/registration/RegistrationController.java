package com.marcus.eventhub.registration;

import com.marcus.eventhub.registration.dto.ParticipantResponse;
import com.marcus.eventhub.registration.dto.RegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/{eventId}")
@Tag(name = "Registrations", description = "Event registrations and participants")
@SecurityRequirement(name = "bearerAuth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/registrations")
    @Operation(summary = "Register for an event")
    public ResponseEntity<RegistrationResponse> register(@PathVariable UUID eventId) {
        RegistrationResponse response = registrationService.register(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/registrations/me")
    @Operation(summary = "Cancel own registration")
    public ResponseEntity<Void> cancel(@PathVariable UUID eventId) {
        registrationService.cancel(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/participants")
    @Operation(summary = "List participants (event owner only)")
    public List<ParticipantResponse> listParticipants(@PathVariable UUID eventId) {
        return registrationService.listParticipants(eventId);
    }
}
