package com.codearena.config;

import com.codearena.entity.User;
import com.codearena.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class AdminUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@codearena.local}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByUsername(adminUsername).ifPresentOrElse(existing -> {
            boolean changed = false;
            if (!passwordEncoder.matches(adminPassword, existing.getPassword())) {
                existing.setPassword(passwordEncoder.encode(adminPassword));
                changed = true;
            }
            if (!"ADMIN".equals(existing.getRole())) {
                existing.setRole("ADMIN");
                changed = true;
            }
            if (changed) {
                userRepository.save(existing);
                log.info("Admin user '{}' updated (password/role synced)", adminUsername);
            }
        }, () -> {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Seeded admin user: username='{}' email='{}' role=ADMIN", adminUsername, adminEmail);
        });
    }
}
