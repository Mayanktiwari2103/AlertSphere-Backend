package com.alertsphere.backend.controller;

import com.alertsphere.backend.model.User;
import com.alertsphere.backend.repository.UserRepository;
import com.alertsphere.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // 🚨 Added for the update method
import java.util.UUID;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwt;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
        }

        if (userRepo.existsByPhoneNumber(user.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number already in use"));
        }

        user.setPassword(encoder.encode(user.getPassword()));
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        User dbUser = userRepo.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String token = jwt.generateToken(dbUser.getEmail(), dbUser.getRole());

        return Map.of(
                "token", token,
                "role", dbUser.getRole(),
                "email", dbUser.getEmail()
        );
    }

    @PostMapping("/google")
    public ResponseEntity<?> handleGoogleLogin(@RequestBody Map<String, String> request) {
        String emailFromGoogle = request.get("email");
        String nameFromGoogle = request.get("name");

        User user = userRepo.findByEmail(emailFromGoogle).orElseGet(() -> {
            System.out.println("New Google User! Registering " + emailFromGoogle);
            User newUser = new User();
            newUser.setEmail(emailFromGoogle);
            newUser.setName(nameFromGoogle);
            newUser.setRole("USER");

            // 🚨 FIX 1: Set placeholder to satisfy @NotBlank validation
            newUser.setPhoneNumber("GOOGLE_" + UUID.randomUUID().toString().substring(0, 8));

            newUser.setPassword(encoder.encode(UUID.randomUUID().toString()));
            return userRepo.save(newUser);
        });

        String token = jwt.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", user
        ));
    }

    // 🚨 FIX 2: Added Update Endpoint for the "Complete Profile" page
    @PutMapping("/update-phone")
    public ResponseEntity<?> updatePhoneNumber(@RequestBody Map<String, String> payload, Principal principal) {
        // principal.getName() retrieves the email from the JWT token automatically
        String email = principal.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newPhone = payload.get("phoneNumber");

        // Check if another user already has this number
        if (userRepo.existsByPhoneNumber(newPhone)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number already in use by another account"));
        }

        user.setPhoneNumber(newPhone);
        User updatedUser = userRepo.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Phone number updated!",
                "user", updatedUser
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        return userRepo.findByEmail(principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}