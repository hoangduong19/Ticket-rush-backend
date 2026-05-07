package com.uet.ticketrush.repos;

import com.uet.ticketrush.enums.SeatStatus;
import com.uet.ticketrush.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Seat s SET s.status = :targetStatus, s.version = s.version + 1 " +
            "WHERE s.seatId IN :seatIds AND s.status = :requiredStatus")
    int updateStatusForSeats(
            @Param("seatIds") List<UUID> seatIds,
            @Param("targetStatus") SeatStatus targetStatus,
            @Param("requiredStatus") SeatStatus requiredStatus
    );

    List<Seat> findByEvent_EventId(UUID eventId);
    void deleteByEvent_EventId(UUID eventId);

    @Modifying
    @Query("DELETE FROM Seat s WHERE s.event.eventId = :eventId")
    void bulkDeleteByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT MIN(s.price) FROM Seat s WHERE s.event.eventId = :eventId AND s.seatType = :seatType")
    BigDecimal findMinPriceByEventId(@Param("eventId") UUID eventId, @Param("seatType") String seatType);

}
