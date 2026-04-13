package com.uet.ticketrush.repos;

import com.uet.ticketrush.enums.HoldStatus;
import com.uet.ticketrush.models.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> {
    @Query("SELECT h FROM SeatHold h JOIN FETCH h.seats WHERE h.holdId = :id")
    Optional<SeatHold> findByIdWithSeats(@Param("id") UUID id);

    Optional<SeatHold> findByUser_UserIdAndEvent_EventIdAndStatus(
            UUID userId,
            UUID eventId,
            HoldStatus status
    );

    List<SeatHold> findAllByStatusAndExpiresAtBefore(HoldStatus status, LocalDateTime dateTime);
}
