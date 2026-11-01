package com.tazkara.identityservice.domain;

import com.tazkara.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) return;

        userRepository.save(User.register(
                "Organizer1",
                "organizer@tazkara.com",
                passwordEncoder.encode("Password123"),
                Role.ORGANIZER,
                "Amman Events"
        ));

        userRepository.save(User.register(
                "Attendee1",
                "attendee@tazkara.com",
                passwordEncoder.encode("Password123"),
                Role.ATTENDEE,
                null
        ));
    }
}