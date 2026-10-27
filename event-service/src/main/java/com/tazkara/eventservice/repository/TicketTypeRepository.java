package com.tazkara.eventservice.repository;

import com.tazkara.eventservice.domain.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketType t WHERE t.event.id = :eventId AND t.id IN :ids ORDER BY t.id")
    List<TicketType> findForUpdate(@Param("eventId") Long eventId, @Param("ids") Collection<Long> ids);
}