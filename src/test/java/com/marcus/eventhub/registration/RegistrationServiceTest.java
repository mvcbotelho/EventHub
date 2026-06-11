package com.marcus.eventhub.registration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcus.eventhub.auth.CurrentUserService;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.event.Event;
import com.marcus.eventhub.event.EventService;
import com.marcus.eventhub.notification.RegistrationNotificationService;
import com.marcus.eventhub.user.User;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private EventRegistrationRepository registrationRepository;

    @Mock
    private EventService eventService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RegistrationNotificationService notificationService;

    @Mock
    private WaitlistService waitlistService;

    private RegistrationService registrationService;
    private RegistrationMetrics registrationMetrics;

    private User owner;
    private User participant;
    private Event event;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        registrationMetrics = new RegistrationMetrics(new SimpleMeterRegistry());
        registrationService = new RegistrationService(
                registrationRepository,
                eventService,
                currentUserService,
                notificationService,
                waitlistService,
                registrationMetrics
        );

        owner = new User("Owner", "owner@test.com", "hash");
        participant = new User("Guest", "guest@test.com", "hash");
        event = new Event(
                "Meetup",
                "Desc",
                "SP",
                Instant.now().plusSeconds(86_400),
                Instant.now().plusSeconds(93_600),
                1,
                owner
        );
        eventId = event.getId();
    }

    @Test
    void registerShouldRejectOwnerSelfRegistration() {
        when(eventService.getEventOrThrow(eventId)).thenReturn(event);
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        assertThatThrownBy(() -> registrationService.register(eventId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Event owners do not need to register for their own events");
    }

    @Test
    void registerShouldRejectDuplicateConfirmedRegistration() {
        EventRegistration existing = new EventRegistration(event, participant);

        when(eventService.getEventOrThrow(eventId)).thenReturn(event);
        when(currentUserService.getCurrentUser()).thenReturn(participant);
        when(registrationRepository.existsOverlappingConfirmedRegistration(
                participant.getId(), eventId, event.getStartDateTime(), event.getEndDateTime()))
                .thenReturn(false);
        when(registrationRepository.findByEventIdAndUserId(eventId, participant.getId()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> registrationService.register(eventId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User is already registered for this event");
    }

    @Test
    void registerShouldRejectFullEvent() {
        when(eventService.getEventOrThrow(eventId)).thenReturn(event);
        when(currentUserService.getCurrentUser()).thenReturn(participant);
        when(registrationRepository.existsOverlappingConfirmedRegistration(
                participant.getId(), eventId, event.getStartDateTime(), event.getEndDateTime()))
                .thenReturn(false);
        when(registrationRepository.findByEventIdAndUserId(eventId, participant.getId()))
                .thenReturn(Optional.empty());
        when(registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED))
                .thenReturn(1L);

        assertThatThrownBy(() -> registrationService.register(eventId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Event is full");
    }

    @Test
    void registerShouldRejectEndedEvent() {
        Event endedEvent = new Event(
                "Past",
                "Desc",
                "SP",
                Instant.now().minusSeconds(7_200),
                Instant.now().minusSeconds(3_600),
                10,
                owner
        );

        when(eventService.getEventOrThrow(endedEvent.getId())).thenReturn(endedEvent);
        when(currentUserService.getCurrentUser()).thenReturn(participant);

        assertThatThrownBy(() -> registrationService.register(endedEvent.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot register for an event that has already ended");
    }

    @Test
    void registerShouldRejectOverlappingSchedule() {
        when(eventService.getEventOrThrow(eventId)).thenReturn(event);
        when(currentUserService.getCurrentUser()).thenReturn(participant);
        when(registrationRepository.existsOverlappingConfirmedRegistration(
                participant.getId(), eventId, event.getStartDateTime(), event.getEndDateTime()))
                .thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(eventId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You already have a confirmed registration for an overlapping event");
    }

    @Test
    void registerShouldNotifyOnSuccess() {
        when(eventService.getEventOrThrow(eventId)).thenReturn(event);
        when(currentUserService.getCurrentUser()).thenReturn(participant);
        when(registrationRepository.existsOverlappingConfirmedRegistration(
                participant.getId(), eventId, event.getStartDateTime(), event.getEndDateTime()))
                .thenReturn(false);
        when(registrationRepository.findByEventIdAndUserId(eventId, participant.getId()))
                .thenReturn(Optional.empty());
        when(registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED))
                .thenReturn(0L);
        when(registrationRepository.save(org.mockito.ArgumentMatchers.any(EventRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.register(eventId);

        verify(notificationService).notifyRegistrationConfirmed(event, participant);
    }
}
