package com.tazkara.identityservice.service;

import com.tazkara.identityservice.domain.User;
import com.tazkara.identityservice.dto.*;
import com.tazkara.identityservice.exception.EmailAlreadyExistsException;
import com.tazkara.identityservice.exception.InvalidCredentialsException;
import com.tazkara.identityservice.exception.InvalidRefreshTokenException;
import com.tazkara.identityservice.mapper.UserMapper;
import com.tazkara.identityservice.repository.UserRepository;
import com.tazkara.identityservice.security.TokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenService refreshTokenService;

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

        return tokensFor(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        Long userId = refreshTokenService.consume(request.refreshToken());
        User user = userRepository.findById(userId).orElseThrow(InvalidRefreshTokenException::new);
        return tokensFor(user);
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private TokenResponse tokensFor(User user) {
        AccessToken access = tokenIssuer.issue(user);
        RefreshTokenDto refresh = refreshTokenService.issue(user.getId());
        return new TokenResponse(access.token(), access.expiresAt(), refresh.token(), refresh.expiresAt());
    }
}