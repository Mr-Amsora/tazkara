package com.tazkara.identityservice.dto;

import java.time.Instant;

public record RefreshTokenDto(String token, Instant expiresAt) {
}
