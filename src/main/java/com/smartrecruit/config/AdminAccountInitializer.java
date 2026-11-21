package com.smartrecruit.config;

import com.smartrecruit.model.Role;
import com.smartrecruit.model.User;
import com.smartrecruit.repository.UserRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the administrator account from ADMIN_EMAIL and ADMIN_PASSWORD (only if it does not exist yet). */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminAccountInitializer(UserRepository users, PasswordEncoder encoder,
                                   @Value("${app.admin.email:}") String email,
                                   @Value("${app.admin.password:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || password.length() < 8) {
            log.info("No administrator created (set ADMIN_EMAIL and an ADMIN_PASSWORD of 8+ characters).");
            return;
        }
        if (users.existsByEmailIgnoreCase(email)) return;
        User admin = new User();
        admin.setName("Administrator");
        admin.setEmail(email);
        admin.setPasswordHash(encoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(Instant.now());
        users.save(admin);
        log.info("Administrator account created for {}", email);
    }
}
