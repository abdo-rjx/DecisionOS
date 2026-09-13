package com.decisionos.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Data
    public static class AuthRequest {
        private String email;
        private String password;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody AuthRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Email and password required"));
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Email already registered"));
        }
        AppUser u = new AppUser();
        u.setEmail(req.getEmail());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("status", "ok", "token", jwtService.generate(u.getEmail())));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest req) {
        return userRepository.findByEmail(req.getEmail())
                .filter(u -> passwordEncoder.matches(req.getPassword(), u.getPasswordHash()))
                .map(u -> ResponseEntity.ok(Map.of("status", "ok", "token", jwtService.generate(u.getEmail()))))
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(Map.of("status", "error", "message", "Invalid credentials")));
    }
}
