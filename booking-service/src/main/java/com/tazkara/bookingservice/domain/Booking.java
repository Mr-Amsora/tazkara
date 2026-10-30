package com.tazkara.bookingservice.domain;

import com.tazkara.bookingservice.domain.refund.RefundPolicy;
import com.tazkara.bookingservice.exception.BookingException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private Long attendeeId;

    @Column(nullable = false, length = 150)
    private String attendeeEmail;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, length = 200)
    private String eventTitle;

    @Column(length = 200)
    private String eventVenue;

    @Column(length = 100)
    private String eventCity;

    @Column(nullable = false)
    private Instant eventStartTime;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    private Instant holdExpiresAt;

    private Instant paidAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CancellationReason cancellationReason;

    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount;

    private boolean ticketsReleased;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BookingLine> lines = new ArrayList<>();

    @Builder
    private Booking(Long attendeeId, String attendeeEmail, Long eventId, String eventTitle,
                    String eventVenue, String eventCity, Instant eventStartTime, String reference,
                    List<BookingLine> lines, Instant holdExpiresAt, Instant now) {
        this.attendeeId = attendeeId;
        this.attendeeEmail = attendeeEmail;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventVenue = eventVenue;
        this.eventCity = eventCity;
        this.eventStartTime = eventStartTime;
        this.reference = reference;
        this.createdAt = now;

        for (BookingLine line : lines) {
            line.attachTo(this);
            this.lines.add(line);
        }

        this.totalAmount = lines.stream()
                .map(BookingLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.signum() == 0) {
            this.status = BookingStatus.CONFIRMED;
            this.paidAt = now;
        } else {
            this.status = BookingStatus.PENDING_PAYMENT;
            this.holdExpiresAt = holdExpiresAt;
        }
    }

    public void ensurePayable(Instant now) {
        if (!status.canMoveTo(BookingStatus.CONFIRMED)) {
            throw new BookingException(HttpStatus.CONFLICT, "BOOKING_NOT_PAYABLE",
                    "Only bookings waiting for payment can be paid");
        }
        if (!now.isBefore(holdExpiresAt)) {
            throw new BookingException(HttpStatus.CONFLICT, "HOLD_EXPIRED",
                    "The hold on these tickets has expired");
        }
    }

    public void confirm(PaymentMethod method, Instant now) {
        ensurePayable(now);
        this.status = BookingStatus.CONFIRMED;
        this.paymentMethod = method;
        this.paidAt = now;
        this.holdExpiresAt = null;
    }

    public void expire() {
        if (!status.canMoveTo(BookingStatus.EXPIRED)) {
            throw new BookingException(HttpStatus.CONFLICT, "BOOKING_NOT_EXPIRABLE",
                    "Only bookings waiting for payment can expire");
        }
        this.status = BookingStatus.EXPIRED;
    }

    public void cancelByAttendee(RefundPolicy policy) {
        if (!status.canMoveTo(BookingStatus.CANCELLED)) {
            throw new BookingException(HttpStatus.CONFLICT, "BOOKING_NOT_CANCELLABLE",
                    "Expired or cancelled bookings cannot be cancelled");
        }
        if (status == BookingStatus.CONFIRMED) {
            if (!policy.allowsCancellation()) {
                throw new BookingException(HttpStatus.UNPROCESSABLE_ENTITY, "CANCELLATION_NOT_ALLOWED",
                        "Confirmed bookings cannot be cancelled less than 48 hours before the event");
            }
            this.refundAmount = policy.refundFor(totalAmount);
        }
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = CancellationReason.BY_ATTENDEE;
        this.holdExpiresAt = null;
    }

    public void markTicketsReleased() {
        this.ticketsReleased = true;
    }
}