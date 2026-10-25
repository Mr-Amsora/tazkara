package com.tazkara.identityservice.mapper;

import com.tazkara.identityservice.domain.User;
import com.tazkara.identityservice.dto.UserResponse;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getOrganizationName());
    }
}