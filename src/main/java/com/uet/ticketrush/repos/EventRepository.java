package com.uet.ticketrush.repos;

import com.uet.ticketrush.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    @Modifying
    @Query("""
    UPDATE Event e
    SET e.status = 'Ended'
    WHERE e.status = 'Published'
    AND e.date < :now
    """)
    int markExpiredEventsEnded(@Param("now") LocalDateTime now);
}
