package com.uet.ticketrush.util;

import com.uet.ticketrush.enums.HoldStatus;
import com.uet.ticketrush.enums.SeatStatus;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.models.SeatHold;
import com.uet.ticketrush.repos.SeatHoldRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeatHoldCleanupTask {

    private final SeatHoldRepository holdRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();

        List<SeatHold> expiredHolds = holdRepository.findAllByStatusAndExpiresAtBefore(
                HoldStatus.Active, now);

        if (!expiredHolds.isEmpty()) {
            for (SeatHold hold : expiredHolds) {
                hold.setStatus(HoldStatus.Expired);

                for (Seat seat : hold.getSeats()) {
                    seat.setStatus(SeatStatus.Available);
                }
            }
            holdRepository.saveAll(expiredHolds);
        }
    }
}