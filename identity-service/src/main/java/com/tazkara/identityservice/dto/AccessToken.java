package com.tazkara.identityservice.dto;

import java.time.Instant;

public record AccessToken(String token, Instant expiresAt) {}
