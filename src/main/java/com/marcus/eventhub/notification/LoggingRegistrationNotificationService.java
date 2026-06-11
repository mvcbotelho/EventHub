package com.marcus.eventhub.notification;

import com.marcus.eventhub.event.Event;
import com.marcus.eventhub.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "eventhub.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingRegistrationNotificationService implements RegistrationNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingRegistrationNotificationService.class);

    @Override
    public void notifyRegistrationConfirmed(Event event, User user) {
        log.info(
                "Registration confirmed: user={} event={} title={} starts={}",
                user.getEmail(),
                event.getId(),
                event.getTitle(),
                event.getStartDateTime()
        );
    }
}
