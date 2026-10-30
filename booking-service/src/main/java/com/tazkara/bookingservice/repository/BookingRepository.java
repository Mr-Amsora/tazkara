package com.tazkara.bookingservice.repository;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT COALESCE(SUM(l.quantity), 0L)
            FROM Booking b JOIN b.lines l
            WHERE b.attendeeId = :attendeeId
              AND b.eventId = :eventId
              AND b.status IN :statuses
            """)
    long countActiveTickets(@Param("attendeeId") Long attendeeId,
                            @Param("eventId") Long eventId,
                            @Param("statuses") Collection<BookingStatus> statuses);

    Optional<Booking> findByReferenceAndAttendeeId(String reference, Long attendeeId);

    Page<Booking> findByAttendeeId(Long attendeeId, Pageable pageable);

    Page<Booking> findByAttendeeIdAndStatus(Long attendeeId, BookingStatus status, Pageable pageable);

    Page<Booking> findByEventId(Long eventId, Pageable pageable);

    Page<Booking> findByEventIdAndStatus(Long eventId, BookingStatus status, Pageable pageable);

    List<Booking> findByEventIdAndStatusOrderByPaidAtAsc(Long eventId, BookingStatus status);

    List<Booking> findByStatusAndHoldExpiresAtBefore(BookingStatus status, Instant time, Pageable pageable);

    List<Booking> findByStatusInAndTicketsReleasedFalse(Collection<BookingStatus> statuses, Pageable pageable);

    @Query("""
            SELECT new com.tazkara.bookingservice.repository.SalesRow(
                   l.ticketTypeId, b.status, SUM(l.quantity), SUM(l.unitPrice * l.quantity ))
            FROM Booking b JOIN b.lines l
            WHERE b.eventId = :eventId AND b.status IN :statuses
            GROUP BY l.ticketTypeId, b.status
            """)
    List<SalesRow> summarizeSales(@Param("eventId") Long eventId,
                                  @Param("statuses") Collection<BookingStatus> statuses);
}