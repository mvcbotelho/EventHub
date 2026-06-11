package com.marcus.eventhub.registration;

import com.marcus.eventhub.auth.CurrentUserService;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ForbiddenException;
import com.marcus.eventhub.event.Event;
import com.marcus.eventhub.event.EventService;
import com.marcus.eventhub.notification.RegistrationNotificationService;
import com.marcus.eventhub.registration.dto.WaitlistEntryResponse;
import com.marcus.eventhub.user.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WaitlistService {

    private final EventWaitlistRepository waitlistRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventService eventService;
    private final CurrentUserService currentUserService;
    private final RegistrationNotificationService notificationService;
    private final RegistrationMetrics registrationMetrics;

    public WaitlistService(
            EventWaitlistRepository waitlistRepository,
            EventRegistrationRepository registrationRepository,
            EventService eventService,
            CurrentUserService currentUserService,
            RegistrationNotificationService notificationService,
            RegistrationMetrics registrationMetrics
    ) {
        this.waitlistRepository = waitlistRepository;
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.registrationMetrics = registrationMetrics;
    }

    @Transactional
    public WaitlistEntryResponse join(UUID eventId) {
        Event event = eventService.getEventOrThrow(eventId);
        User currentUser = currentUserService.getCurrentUser();

        assertNotOwner(event, currentUser);
        assertEventNotEnded(event);

        if (waitlistRepository.existsByEventIdAndUserId(eventId, currentUser.getId())) {
            throw new BusinessException("User is already on the waitlist for this event");
        }

        Optional<EventRegistration> existingRegistration =
                registrationRepository.findByEventIdAndUserId(eventId, currentUser.getId());

        if (existingRegistration.isPresent()
                && existingRegistration.get().getStatus() == RegistrationStatus.CONFIRMED) {
            throw new BusinessException("User is already registered for this event");
        }

        long confirmedCount = registrationRepository.countByEventIdAndStatus(
                eventId,
                RegistrationStatus.CONFIRMED
        );

        if (confirmedCount < event.getMaxParticipants()) {
            throw new BusinessException("Event is not full; register directly instead");
        }

        EventWaitlistEntry entry = waitlistRepository.save(new EventWaitlistEntry(event, currentUser));
        return WaitlistEntryResponse.from(entry);
    }

    @Transactional
    public void leave(UUID eventId) {
        User currentUser = currentUserService.getCurrentUser();

        EventWaitlistEntry entry = waitlistRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .orElseThrow(() -> new BusinessException("Waitlist entry not found"));

        waitlistRepository.delete(entry);
    }

    public List<WaitlistEntryResponse> list(UUID eventId) {
        Event event = eventService.getEventOrThrow(eventId);
        User currentUser = currentUserService.getCurrentUser();

        if (!event.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the event owner can view the waitlist");
        }

        return waitlistRepository.findByEventIdOrderByJoinedAtAsc(eventId).stream()
                .map(WaitlistEntryResponse::from)
                .toList();
    }

    @Transactional
    public void promoteNext(UUID eventId) {
        Event event = eventService.getEventOrThrow(eventId);

        long confirmedCount = registrationRepository.countByEventIdAndStatus(
                eventId,
                RegistrationStatus.CONFIRMED
        );

        if (confirmedCount >= event.getMaxParticipants()) {
            return;
        }

        Optional<EventWaitlistEntry> nextEntry = waitlistRepository.findFirstByEventIdOrderByJoinedAtAsc(eventId);
        if (nextEntry.isEmpty()) {
            return;
        }

        EventWaitlistEntry entry = nextEntry.get();
        User user = entry.getUser();

        if (hasScheduleConflict(user.getId(), event)) {
            waitlistRepository.delete(entry);
            promoteNext(eventId);
            return;
        }

        EventRegistration registration = registrationRepository
                .findByEventIdAndUserId(eventId, user.getId())
                .orElseGet(() -> new EventRegistration(event, user));

        registration.setStatus(RegistrationStatus.CONFIRMED);
        registrationRepository.save(registration);
        waitlistRepository.delete(entry);

        registrationMetrics.trackConfirmed();
        notificationService.notifyRegistrationConfirmed(event, user);
    }

    private boolean hasScheduleConflict(UUID userId, Event event) {
        return registrationRepository.existsOverlappingConfirmedRegistration(
                userId,
                event.getId(),
                event.getStartDateTime(),
                event.getEndDateTime()
        );
    }

    private void assertNotOwner(Event event, User currentUser) {
        if (event.getOwner().getId().equals(currentUser.getId())) {
            throw new BusinessException("Event owners do not need to join the waitlist for their own events");
        }
    }

    private void assertEventNotEnded(Event event) {
        if (event.getEndDateTime().isBefore(Instant.now())) {
            throw new BusinessException("Cannot join the waitlist for an event that has already ended");
        }
    }
}
