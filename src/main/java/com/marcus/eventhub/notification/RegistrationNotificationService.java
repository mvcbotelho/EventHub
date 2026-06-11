package com.marcus.eventhub.notification;

import com.marcus.eventhub.event.Event;
import com.marcus.eventhub.user.User;

public interface RegistrationNotificationService {

    void notifyRegistrationConfirmed(Event event, User user);
}
