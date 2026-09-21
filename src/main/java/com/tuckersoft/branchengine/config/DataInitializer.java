package com.tuckersoft.branchengine.config;

import com.tuckersoft.branchengine.user.User;
import com.tuckersoft.branchengine.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Crea al administrador del .env al arrancar, si todavia no existe. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String name;
    private final String email;
    private final String password;

    public DataInitializer(UserRepository users, PasswordEncoder encoder,
                           @Value("${app.admin.display-name}") String name,
                           @Value("${app.admin.email}") String email,
                           @Value("${app.admin.password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.name = name;
        this.email = email.trim().toLowerCase();
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (users.existsByEmail(email)) {
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(encoder.encode(password));
        admin.setDisplayName(name);
        admin.setRole(User.ROLE_ADMIN);
        admin.setCreatedAt(Instant.now());
        users.save(admin);
    }
}
