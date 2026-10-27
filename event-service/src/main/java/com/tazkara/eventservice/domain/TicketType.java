package com.tazkara.eventservice.domain;

import com.tazkara.eventservice.exception.TicketsUnavailableException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_types")
@Getter
@NoArgsConstructor
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reservedCount;

    public TicketType(String name, BigDecimal price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.reservedCount = 0;
    }

    public void attachTo(Event event) {
        this.event = event;
    }

    public int getAvailableTickets() {
        return Math.max(0, this.quantity - this.reservedCount);
    }

    public boolean isSoldOut() {
        return getAvailableTickets() == 0;
    }

    public void reserve(int amount) {
        if (amount > getAvailableTickets()) {
            throw new TicketsUnavailableException(name);
        }
        reservedCount += amount;
    }

    public void release(int amount) {
        reservedCount = Math.max(0, reservedCount - amount);
    }
}