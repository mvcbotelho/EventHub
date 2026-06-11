package com.marcus.eventhub.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcus.eventhub.auth.CurrentUserService;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ForbiddenException;
import com.marcus.eventhub.event.dto.CreateEventRequest;
import com.marcus.eventhub.event.dto.UpdateEventRequest;
import com.marcus.eventhub.registration.EventRegistrationRepository;
import com.marcus.eventhub.registration.RegistrationStatus;
import com.marcus.eventhub.user.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRegistrationRepository registrationRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private EventService eventService;

    private User owner;
    private User otherUser;
    private Event event;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "owner@test.com", "hash");
        otherUser = new User("Other", "other@test.com", "hash");
        event = new Event(
                "Meetup",
                "Desc",
                "SP",
                Instant.parse("2026-12-15T19:00:00Z"),
                Instant.parse("2026-12-15T21:00:00Z"),
                10,
                owner
        );
    }

    @Test
    void createShouldAssignCurrentUserAsOwner() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateEventRequest request = new CreateEventRequest(
                "Meetup",
                "Desc",
                "SP",
                Instant.parse("2026-12-20T19:00:00Z"),
                Instant.parse("2026-12-20T21:00:00Z"),
                10
        );

        var response = eventService.create(request);

        assertThat(response.title()).isEqualTo("Meetup");
        assertThat(response.ownerId()).isEqualTo(owner.getId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createShouldRejectEndDateBeforeStartDate() {
        CreateEventRequest request = new CreateEventRequest(
                "Meetup",
                "Desc",
                "SP",
                Instant.parse("2026-12-20T21:00:00Z"),
                Instant.parse("2026-12-20T19:00:00Z"),
                10
        );

        assertThatThrownBy(() -> eventService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("End date and time cannot be before start date and time");
    }

    @Test
    void updateShouldAllowOwnerWhenNoRegistrants() {
        when(eventRepository.findByIdWithOwner(event.getId())).thenReturn(Optional.of(event));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.CONFIRMED))
                .thenReturn(0L);

        UpdateEventRequest request = new UpdateEventRequest(
                "Updated",
                "Desc",
                "RJ",
                Instant.parse("2026-12-15T19:00:00Z"),
                Instant.parse("2026-12-15T21:00:00Z"),
                10
        );

        var response = eventService.update(event.getId(), request);

        assertThat(response.title()).isEqualTo("Updated");
        assertThat(response.location()).isEqualTo("RJ");
    }

    @Test
    void updateShouldRejectWhenEventHasConfirmedRegistrants() {
        when(eventRepository.findByIdWithOwner(event.getId())).thenReturn(Optional.of(event));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.CONFIRMED))
                .thenReturn(2L);

        UpdateEventRequest request = new UpdateEventRequest(
                "Updated",
                "Desc",
                "RJ",
                Instant.parse("2026-12-15T19:00:00Z"),
                Instant.parse("2026-12-15T21:00:00Z"),
                10
        );

        assertThatThrownBy(() -> eventService.update(event.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot update an event that has confirmed registrations");
    }

    @Test
    void deleteShouldSoftDeleteForOwner() {
        when(eventRepository.findByIdWithOwner(event.getId())).thenReturn(Optional.of(event));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(eventRepository.save(event)).thenReturn(event);

        eventService.delete(event.getId());

        assertThat(event.getDeletedAt()).isNotNull();
        verify(eventRepository).save(event);
    }

    @Test
    void updateShouldRejectNonOwner() {
        when(eventRepository.findByIdWithOwner(event.getId())).thenReturn(Optional.of(event));
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);

        UpdateEventRequest request = new UpdateEventRequest(
                "Updated",
                "Desc",
                "RJ",
                Instant.parse("2026-12-15T19:00:00Z"),
                Instant.parse("2026-12-15T21:00:00Z"),
                10
        );

        assertThatThrownBy(() -> eventService.update(event.getId(), request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the event owner can perform this action");
    }
}
