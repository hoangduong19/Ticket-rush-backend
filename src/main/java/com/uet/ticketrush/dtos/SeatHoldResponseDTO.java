package com.uet.ticketrush.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record SeatHoldResponseDTO(
        UUID holdId,
        LocalDateTime expiresAt,
        long secondsLeft,
        List<SeatDTO> seats,
        BigDecimal totalPrice
) {
    public record SeatDTO(
            Integer seatNumber,
            BigDecimal price,
            String section
    ) {}
}
