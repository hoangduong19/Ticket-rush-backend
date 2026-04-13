package com.uet.ticketrush.services;


import com.uet.ticketrush.dtos.CheckoutSuccessEvent;
import com.uet.ticketrush.enums.HoldStatus;
import com.uet.ticketrush.enums.SeatStatus;
import com.uet.ticketrush.exceptions.TicketRushException;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.models.SeatHold;
import com.uet.ticketrush.repos.SeatHoldRepository;
import com.uet.ticketrush.repos.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final ApplicationEventPublisher eventPublisher;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository holdRepository;

    @Transactional
    public void checkout(UUID holdId) {
        SeatHold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin giữ chỗ"));

        if (hold.getStatus() != HoldStatus.Active || hold.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TicketRushException("Phiên làm việc không hợp lệ", HttpStatus.GONE);
        }

        Set<Seat> seats = hold.getSeats();
        List<UUID> seatIds = seats.stream()
                .map(Seat::getSeatId)
                .toList();

        int updatedCount = seatRepository.updateStatusForSeats(seatIds, SeatStatus.Sold, SeatStatus.Locked);
        if (updatedCount != seatIds.size()) {
            throw new TicketRushException("Xung đột dữ liệu ghế", HttpStatus.CONFLICT);
        }

        hold.setStatus(HoldStatus.Completed);
        holdRepository.save(hold);

        eventPublisher.publishEvent(new CheckoutSuccessEvent(hold, seats));
    }
}
