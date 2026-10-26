
package com.fleetflow.fleetflowbackend.controller;
import com.fleetflow.fleetflowbackend.security.JwtService;
import com.fleetflow.fleetflowbackend.entity.User;
import com.fleetflow.fleetflowbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "DRIVER",
            "OPERATIONS",
            "FLEET_MANAGER",
            "ADMIN"
    );

    // Change this to your own private random value.
    private static final String SETUP_KEY =
            "ReplaceWithYourPrivateSetupKey123!";

    public AuthController(
            AuthService authService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            String requestedRole = request.get("role");

            if (name == null || name.isBlank()
                    || email == null || email.isBlank()
                    || password == null || password.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message",
                        "Name and email are required; password must contain at least 8 characters"
                ));
            }

            User user = authService.register(
                    name, email, password, requestedRole);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<UserResponse> users = authService.findAllUsers()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || email.isBlank()
                    || password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Email and password are required"));
            }

            User user = authService.findByEmail(email);
            String encodedPassword = user.getPassword();

            if (encodedPassword == null
                    || !encodedPassword.matches("^\\$2[aby]\\$.{56}$")) {
                return ResponseEntity.status(
                        HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "message",
                        "Stored password is not a valid BCrypt hash. Reset this account's password."
                ));
            }

            if (!passwordEncoder.matches(password, encodedPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message", "Invalid email or password"));
            }

            String role = user.getRole();

            if (role == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Invalid user role"));
            }

            role = role.trim().toUpperCase(Locale.ROOT);

            if (!ALLOWED_ROLES.contains(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Invalid user role"));
            }

            String token = jwtService.generateToken(
                    user.getEmail(),
                    role
            );

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tokenType", "Bearer",
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", role
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "Invalid email or password"));
        }
    }

    // TEMPORARY ADMIN PASSWORD RESET
    // Remove this endpoint after resetting the Admin password.
    @PostMapping("/setup-admin-password")
    public ResponseEntity<?> setupAdminPassword(
            @RequestBody Map<String, String> request) {
        try {
            String setupKey = request.get("setupKey");
            String password = request.get("password");

            if (setupKey == null || !SETUP_KEY.equals(setupKey)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Setup not authorized"));
            }

            if (password == null || password.length() < 12) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message",
                        "Password must contain at least 12 characters"));
            }

            User admin = authService.findByEmail("admin@fleetflow.com");

            if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message", "The account is not an Admin account"));
            }

            admin.setPassword(passwordEncoder.encode(password));
            authService.saveUser(admin);

            return ResponseEntity.ok(Map.of(
                    "message", "Admin password updated successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
