package com.fleetflow.fleetflowbackend.service;
import com.fleetflow.fleetflowbackend.entity.Driver;
import com.fleetflow.fleetflowbackend.repository.DriverRepository;
import com.fleetflow.fleetflowbackend.entity.User;
import com.fleetflow.fleetflowbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DriverRepository driverRepository;
    private static final Set<String> ALLOWED_ROLES = Set.of(
            "DRIVER",
            "OPERATIONS",
            "FLEET_MANAGER",
            "ADMIN"
    );

    public AuthService(
            UserRepository userRepository,
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(
            String name,
            String email,
            String password,
            String requestedRole) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists");
        }

        String role = normalizeRole(requestedRole);

        // Public registration must not allow self-assigned Admin accounts.
        if ("ADMIN".equals(role)) {
            throw new IllegalArgumentException(
                    "Admin accounts must be created by an administrator");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(
                email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Invalid email or password"));
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    private String normalizeRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return "DRIVER";
        }

        String role = requestedRole.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');

        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role selected");
        }

        return role;
    }
}
