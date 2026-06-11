package com.marcus.eventhub.registration;

import com.marcus.eventhub.registration.dto.WaitlistEntryResponse;
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
@RequestMapping("/events/{eventId}/waitlist")
@Tag(name = "Waitlist", description = "Event waitlist when capacity is reached")
@SecurityRequirement(name = "bearerAuth")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    @Operation(summary = "Join the waitlist", description = "Only available when the event is full.")
    public ResponseEntity<WaitlistEntryResponse> join(@PathVariable UUID eventId) {
        WaitlistEntryResponse response = waitlistService.join(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Leave the waitlist")
    public ResponseEntity<Void> leave(@PathVariable UUID eventId) {
        waitlistService.leave(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List waitlist entries (event owner only)")
    public List<WaitlistEntryResponse> list(@PathVariable UUID eventId) {
        return waitlistService.list(eventId);
    }
}
