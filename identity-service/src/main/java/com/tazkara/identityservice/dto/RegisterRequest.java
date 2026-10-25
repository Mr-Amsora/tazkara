package com.tazkara.identityservice.dto;

import com.tazkara.identityservice.domain.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 100)
        String fullName,

        @NotBlank @Email @Size(max = 320)
        String email,

        @NotBlank
        @Size(min = 8, max = 32, message = "must be between 8 and 32 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain at least one letter and one digit")
        String password,

        @NotNull
        Role role,

        @Size(max = 100)
        String organizationName
) {
}
