package com.tazkara.identityservice.dto;

import com.tazkara.identityservice.domain.Role;

public record UserResponse(
        Long id,

        String fullName,

        String email,

        Role role,

        String organizationName
) {}