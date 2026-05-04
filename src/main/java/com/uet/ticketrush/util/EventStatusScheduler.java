package com.uet.ticketrush.util;

import com.uet.ticketrush.repos.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void markExpiredEventsAsEnded() {
        int updated = eventRepository.markExpiredEventsEnded(LocalDateTime.now());
        if (updated > 0) {
            log.info("[Scheduler] Marked {} event(s) as ENDED", updated);
        }
    }
}