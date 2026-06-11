package com.marcus.eventhub.admin;

import com.marcus.eventhub.admin.dto.AdminStatsResponse;
import com.marcus.eventhub.admin.dto.AdminUserResponse;
import com.marcus.eventhub.admin.dto.UpdateUserRoleRequest;
import com.marcus.eventhub.auth.CurrentUserService;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.common.exception.ResourceNotFoundException;
import com.marcus.eventhub.event.Event;
import com.marcus.eventhub.event.EventRepository;
import com.marcus.eventhub.registration.EventRegistrationRepository;
import com.marcus.eventhub.user.User;
import com.marcus.eventhub.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final CurrentUserService currentUserService;

    public AdminService(
            UserRepository userRepository,
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.currentUserService = currentUserService;
    }

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional
    public AdminUserResponse updateUserRole(UUID userId, UpdateUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User currentUser = currentUserService.getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new BusinessException("Admins cannot change their own role");
        }

        user.setRole(request.role());
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.softDelete();
        eventRepository.save(event);
    }

    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
                userRepository.count(),
                eventRepository.count(),
                registrationRepository.count()
        );
    }
}
