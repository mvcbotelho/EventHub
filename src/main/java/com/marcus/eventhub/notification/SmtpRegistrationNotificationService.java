package com.marcus.eventhub.notification;

import com.marcus.eventhub.event.Event;
import com.marcus.eventhub.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "eventhub.mail.enabled", havingValue = "true")
public class SmtpRegistrationNotificationService implements RegistrationNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmtpRegistrationNotificationService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public SmtpRegistrationNotificationService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void notifyRegistrationConfirmed(Event event, User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(user.getEmail());
        message.setSubject("Registration confirmed: " + event.getTitle());
        message.setText("""
                Hello %s,

                Your registration for "%s" has been confirmed.

                Location: %s
                Starts: %s
                Ends: %s

                See you there!
                EventHub
                """.formatted(
                user.getName(),
                event.getTitle(),
                event.getLocation(),
                event.getStartDateTime(),
                event.getEndDateTime()
        ));

        mailSender.send(message);
        log.info("Registration email sent to {} for event {}", user.getEmail(), event.getId());
    }
}
