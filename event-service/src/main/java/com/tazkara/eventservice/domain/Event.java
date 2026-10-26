package com.tazkara.eventservice.domain;

import com.tazkara.eventservice.exception.InvalidEventException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long organizerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false, length = 200)
    private String venue;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private Instant startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketType> ticketTypes = new ArrayList<>();

    private Event(Long organizerId, String title, String description,
                  String venue, String city, Instant startTime) {
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.city = city;
        this.startTime = startTime;
        this.status = EventStatus.DRAFT;
    }

    public static Event create(Long organizerId, String title, String description,
                               String venue, String city, Instant startTime,
                               List<TicketType> ticketTypes, Instant now) {
        validateStartTime(startTime, now);
        validateTicketTypes(ticketTypes);

        Event event = new Event(organizerId, title, description, venue, city, startTime);
        for (TicketType ticketType : ticketTypes) {
            ticketType.attachTo(event);
            event.ticketTypes.add(ticketType);
        }
        return event;
    }


    private static void validateStartTime(Instant startTime, Instant now) {
        if (startTime.isBefore(now.plus(24, ChronoUnit.HOURS))) {
            throw new InvalidEventException("Event must start at least 24 hours from now");
        }
    }

    private static void validateTicketTypes(List<TicketType> ticketTypes) {
        if (ticketTypes.isEmpty() || ticketTypes.size() > 5) {
            throw new InvalidEventException("An event needs between 1 and 5 ticket types");
        }
        long distinctNames = ticketTypes.stream()
                .map(type -> type.getName().toLowerCase(Locale.ROOT))
                .distinct()
                .count();
        if (distinctNames != ticketTypes.size()) {
            throw new InvalidEventException("Ticket type names must be unique within an event");
        }
    }
}