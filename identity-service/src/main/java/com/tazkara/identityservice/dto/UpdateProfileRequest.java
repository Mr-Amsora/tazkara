package com.tazkara.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100)
        String fullName,

        @Size(max = 100)
        String organizationName
) {}