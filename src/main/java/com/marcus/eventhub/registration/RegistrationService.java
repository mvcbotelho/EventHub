package com.marcus.eventhub.registration;

import com.marcus.eventhub.auth.CurrentUserService;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ForbiddenException;
import com.marcus.eventhub.event.Event;
import com.marcus.eventhub.event.EventService;
import com.marcus.eventhub.notification.RegistrationNotificationService;
import com.marcus.eventhub.registration.dto.ParticipantResponse;
import com.marcus.eventhub.registration.dto.RegistrationResponse;
import com.marcus.eventhub.user.User;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventService eventService;
    private final CurrentUserService currentUserService;
    private final RegistrationNotificationService notificationService;
    private final Counter confirmedRegistrationsCounter;

    public RegistrationService(
            EventRegistrationRepository registrationRepository,
            EventService eventService,
            CurrentUserService currentUserService,
            RegistrationNotificationService notificationService,
            MeterRegistry meterRegistry
    ) {
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.confirmedRegistrationsCounter = Counter.builder("eventhub.registrations.confirmed")
                .description("Confirmed event registrations")
                .register(meterRegistry);
    }

    @Transactional
    public RegistrationResponse register(UUID eventId) {
        Event event = eventService.getEventOrThrow(eventId);
        User currentUser = currentUserService.getCurrentUser();

        if (event.getOwner().getId().equals(currentUser.getId())) {
            throw new BusinessException("Event owners do not need to register for their own events");
        }

        if (event.getEndDateTime().isBefore(Instant.now())) {
            throw new BusinessException("Cannot register for an event that has already ended");
        }

        validateScheduleConflict(currentUser.getId(), event);

        var existingRegistration = registrationRepository.findByEventIdAndUserId(eventId, currentUser.getId());

        if (existingRegistration.isPresent()) {
            EventRegistration registration = existingRegistration.get();
            if (registration.getStatus() == RegistrationStatus.CONFIRMED) {
                throw new BusinessException("User is already registered for this event");
            }
            validateCapacity(event);
            registration.setStatus(RegistrationStatus.CONFIRMED);
            notifyAndTrack(event, currentUser);
            return RegistrationResponse.from(registration);
        }

        validateCapacity(event);
        EventRegistration registration = registrationRepository.save(new EventRegistration(event, currentUser));
        notifyAndTrack(event, currentUser);
        return RegistrationResponse.from(registration);
    }

    @Transactional
    public void cancel(UUID eventId) {
        User currentUser = currentUserService.getCurrentUser();

        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .orElseThrow(() -> new BusinessException("Registration not found"));

        if (registration.getStatus() == RegistrationStatus.CANCELED) {
            throw new BusinessException("Registration is already canceled");
        }

        registration.setStatus(RegistrationStatus.CANCELED);
    }

    public List<ParticipantResponse> listParticipants(UUID eventId) {
        Event event = eventService.getEventOrThrow(eventId);
        User currentUser = currentUserService.getCurrentUser();

        if (!event.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the event owner can view participants");
        }

        return registrationRepository.findByEventIdAndStatusOrderByRegisteredAtAsc(eventId, RegistrationStatus.CONFIRMED).stream()
                .map(ParticipantResponse::from)
                .toList();
    }

    private void validateCapacity(Event event) {
        long confirmedCount = registrationRepository.countByEventIdAndStatus(
                event.getId(),
                RegistrationStatus.CONFIRMED
        );

        if (confirmedCount >= event.getMaxParticipants()) {
            throw new BusinessException("Event is full");
        }
    }

    private void validateScheduleConflict(UUID userId, Event event) {
        boolean hasConflict = registrationRepository.existsOverlappingConfirmedRegistration(
                userId,
                event.getId(),
                event.getStartDateTime(),
                event.getEndDateTime()
        );

        if (hasConflict) {
            throw new BusinessException("You already have a confirmed registration for an overlapping event");
        }
    }

    private void notifyAndTrack(Event event, User user) {
        confirmedRegistrationsCounter.increment();
        notificationService.notifyRegistrationConfirmed(event, user);
    }
}
