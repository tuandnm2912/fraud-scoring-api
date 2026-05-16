package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    @Value("${app.jwt.refresh-token-days:7}")
    private long refreshTokenDays;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        return issueTokens(user, ipAddress, userAgent, "AUTH_REGISTER");
    }

    @Transactional
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

        return issueTokens(user, ipAddress, userAgent, "AUTH_LOGIN");
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String token = request.refreshToken().trim();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            auditLogService.log(refreshToken.getUser(), "AUTH_REFRESH", "FAIL", "Refresh token expired or revoked", ipAddress, userAgent);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return issueTokens(refreshToken.getUser(), ipAddress, userAgent, "AUTH_REFRESH");
    }

    @Transactional
    public void logout(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String token = request.refreshToken().trim();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        }

        auditLogService.log(refreshToken.getUser(), "AUTH_LOGOUT", "SUCCESS", "User logged out", ipAddress, userAgent);
    }

    private AuthResponse issueTokens(User user, String ipAddress, String userAgent, String action) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = generateRefreshToken();

        RefreshToken storedRefreshToken = new RefreshToken();
        storedRefreshToken.setUser(user);
        storedRefreshToken.setTokenHash(hashToken(refreshToken));
        storedRefreshToken.setExpiresAt(Instant.now().plus(Duration.ofDays(refreshTokenDays)));
        storedRefreshToken.setCreatedAt(Instant.now());
        refreshTokenRepository.save(storedRefreshToken);

        auditLogService.log(user, action, "SUCCESS", "Issued access and refresh tokens", ipAddress, userAgent);
        return new AuthResponse(accessToken, refreshToken);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash token", ex);
        }
    }
}