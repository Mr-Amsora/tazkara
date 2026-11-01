package com.tazkara.eventservice.domain;

import com.tazkara.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DemoData implements CommandLineRunner {

    private final EventRepository eventRepository;

    @Override
    public void run(String... args) {

        if (eventRepository.count() > 0) return;

        Instant now = Instant.now();

        Event event = Event.create(
                1L,
                "Amman Dance Night",
                "A big dance event in Amman.",
                "Roman Theatre",
                "Amman",
                now.plus(60, ChronoUnit.DAYS),
                List.of(
                        new TicketType("Standard", new BigDecimal("25"), 200),
                        new TicketType("VIP", new BigDecimal("80"), 50)
                ),
                now
        );

        event.setStatus(EventStatus.PUBLISHED);

        eventRepository.save(event);
    }
}