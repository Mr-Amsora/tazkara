package com.tazkara.identityservice.service;

import com.tazkara.identityservice.domain.Role;
import com.tazkara.identityservice.domain.User;
import com.tazkara.identityservice.dto.UpdateProfileRequest;
import com.tazkara.identityservice.dto.UserResponse;
import com.tazkara.identityservice.exception.InvalidCredentialsException;
import com.tazkara.identityservice.mapper.UserMapper;
import com.tazkara.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public UserResponse getProfile(Long userId) {
        return UserMapper.toResponse(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);

        String newFullName = request.fullName();
        if(newFullName != null && !newFullName.trim().isEmpty())  {
            user.setFullName(newFullName.trim());
        }

        String organizationName = request.organizationName();
        boolean isOrganizer = user.getRole() == Role.ORGANIZER;
        if (isOrganizer && organizationName != null && !organizationName.trim().isEmpty()) {
            user.setOrganizationName(organizationName.trim());
        }

        return UserMapper.toResponse(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
    }
}