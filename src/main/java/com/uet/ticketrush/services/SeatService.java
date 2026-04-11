package com.uet.ticketrush.services;

import com.uet.ticketrush.enums.SeatStatus;
import com.uet.ticketrush.exceptions.TicketRushException;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.repos.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;
    public void lockSeats(List<UUID> seatIds) {
        int updated = seatRepository.updateStatusForSeats(seatIds, SeatStatus.Locked, SeatStatus.Available);

        if (updated != seatIds.size()) {
            List<Seat> failedSeats = seatRepository.findAllById(seatIds);
            List<String> failedNames = failedSeats.stream()
                    .filter(s -> !s.isAvailable())
                    .map(s -> "Hàng " + s.getRowNumber() + " Ghế " + s.getSeatNumber())
                    .toList();

            throw new TicketRushException("Ghế đã được book: " + String.join(", ", failedNames), HttpStatus.CONFLICT);
        }
    }

}
