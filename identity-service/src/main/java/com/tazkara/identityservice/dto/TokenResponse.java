package com.tazkara.identityservice.dto;

import java.time.Instant;

public record TokenResponse(

        String token,

        Instant expiresAt,

        String refreshToken,

        Instant refreshExpiresAt
) {}