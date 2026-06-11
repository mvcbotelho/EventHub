package com.marcus.eventhub.admin;

import com.marcus.eventhub.user.User;
import com.marcus.eventhub.user.UserRepository;
import com.marcus.eventhub.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "eventhub.admin", name = "bootstrap-email")
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapEmail;
    private final String bootstrapPassword;
    private final String bootstrapName;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${eventhub.admin.bootstrap-email}") String bootstrapEmail,
            @Value("${eventhub.admin.bootstrap-password}") String bootstrapPassword,
            @Value("${eventhub.admin.bootstrap-name:EventHub Admin}") String bootstrapName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapPassword = bootstrapPassword;
        this.bootstrapName = bootstrapName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
            return;
        }

        userRepository.findByEmail(bootstrapEmail).ifPresentOrElse(
                this::promoteToAdminIfNeeded,
                this::createAdminUser
        );
    }

    private void promoteToAdminIfNeeded(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            user.setRole(UserRole.ADMIN);
            log.info("Promoted existing user {} to ADMIN", bootstrapEmail);
        }
    }

    private void createAdminUser() {
        User admin = new User(
                bootstrapName,
                bootstrapEmail,
                passwordEncoder.encode(bootstrapPassword),
                UserRole.ADMIN
        );
        userRepository.save(admin);
        log.info("Created bootstrap admin user {}", bootstrapEmail);
    }
}
