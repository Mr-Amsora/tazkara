package com.tazkara.bookingservice.repository;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

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
}