package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        auditLogService.log(user, "AUTH_REGISTER", "SUCCESS", "User registered", ipAddress, userAgent);
        return new AuthResponse(jwtService.generateAccessToken(user.getId(), user.getEmail()));
    }

    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            auditLogService.log(null, "AUTH_LOGIN", "FAIL", "Invalid credentials", ipAddress, userAgent);
            throw new IllegalArgumentException("Invalid credentials");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (Exception ex) {
            auditLogService.log(user, "AUTH_LOGIN", "FAIL", "Invalid credentials", ipAddress, userAgent);
            throw new IllegalArgumentException("Invalid credentials");
        }

        auditLogService.log(user, "AUTH_LOGIN", "SUCCESS", "User logged in", ipAddress, userAgent);
        return new AuthResponse(jwtService.generateAccessToken(user.getId(), user.getEmail()));
    }
}