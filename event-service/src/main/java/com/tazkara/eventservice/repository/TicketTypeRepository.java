package com.tazkara.eventservice.repository;

import com.tazkara.eventservice.domain.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TicketType> findByEventIdAndIdInOrderById(
            Long eventId,
            Collection<Long> ids
    );
}