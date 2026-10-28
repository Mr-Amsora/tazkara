package com.tazkara.bookingservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(nullable = false)
    private Long ticketTypeId;

    @Column(nullable = false, length = 50)
    private String ticketTypeName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    public BookingLine(Long ticketTypeId, String ticketTypeName, BigDecimal unitPrice, int quantity) {
        this.ticketTypeId = ticketTypeId;
        this.ticketTypeName = ticketTypeName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    void attachTo(Booking booking) {
        this.booking = booking;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}