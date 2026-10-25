package com.tazkara.identityservice.service;

import com.tazkara.identityservice.domain.User;
import com.tazkara.identityservice.dto.RegisterRequest;
import com.tazkara.identityservice.dto.UserResponse;
import com.tazkara.identityservice.exception.EmailAlreadyExistsException;
import com.tazkara.identityservice.mapper.UserMapper;
import com.tazkara.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.register(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.role(),
                request.organizationName());

        try {
            User saved = userRepository.saveAndFlush(user);
            log.info("Registered user id={} role={}", saved.getId(), saved.getRole());
            return UserMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException();
        }
    }
}