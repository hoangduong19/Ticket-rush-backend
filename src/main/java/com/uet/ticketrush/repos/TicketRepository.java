package com.uet.ticketrush.repos;

import com.uet.ticketrush.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByUser_UserIdOrderByPurchaseDateDesc(UUID userId);

    @Modifying
    @Query("DELETE FROM Ticket t WHERE t.seat.event.eventId = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}


