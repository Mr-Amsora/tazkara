package com.tazkara.identityservice.service;

import com.tazkara.identityservice.domain.User;
import com.tazkara.identityservice.dto.LoginRequest;
import com.tazkara.identityservice.dto.RegisterRequest;
import com.tazkara.identityservice.dto.TokenResponse;
import com.tazkara.identityservice.dto.UserResponse;
import com.tazkara.identityservice.exception.EmailAlreadyExistsException;
import com.tazkara.identityservice.exception.InvalidCredentialsException;
import com.tazkara.identityservice.mapper.UserMapper;
import com.tazkara.identityservice.repository.UserRepository;
import com.tazkara.identityservice.security.TokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

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
            return UserMapper.toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException();
        }
    }

    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);

        return tokenIssuer.issue(user);
    }
}