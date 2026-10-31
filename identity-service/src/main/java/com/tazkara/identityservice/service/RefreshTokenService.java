package com.tazkara.identityservice.service;

import com.tazkara.identityservice.domain.RefreshToken;
import com.tazkara.identityservice.dto.RefreshTokenDto;
import com.tazkara.identityservice.exception.InvalidRefreshTokenException;
import com.tazkara.identityservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${app.jwt.refresh-expiration-days}")
    private long expirationDays;

    public RefreshTokenDto issue(Long userId) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        Instant expiresAt = Instant.now().plus(Duration.ofDays(expirationDays));

        repository.save(new RefreshToken(hash(token), userId, expiresAt));

        return new RefreshTokenDto(token, expiresAt);
    }

    @Transactional
    public Long consume(String token) {
        RefreshToken stored = repository.findByTokenHash(hash(token))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked() || stored.isExpired(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        stored.setRevoked(true);

        return stored.getUserId();
    }

    @Transactional
    public void revoke(String token) {
        repository.findByTokenHash(hash(token))
                .ifPresent(stored -> stored.setRevoked(true));
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}