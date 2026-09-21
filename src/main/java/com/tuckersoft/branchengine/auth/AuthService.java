package com.tuckersoft.branchengine.auth;

import com.tuckersoft.branchengine.error.ApiException;
import com.tuckersoft.branchengine.security.JwtService;
import com.tuckersoft.branchengine.user.User;
import com.tuckersoft.branchengine.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("El email ya esta registrado");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setRole(User.ROLE_USER);
        user.setCreatedAt(Instant.now());
        users.save(user);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email().trim().toLowerCase())
                .filter(u -> encoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> ApiException.unauthorized("Credenciales incorrectas"));
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return AuthResponse.of(jwt.generate(user.getEmail()), user.getEmail(),
                user.getDisplayName(), user.getRole());
    }
}
