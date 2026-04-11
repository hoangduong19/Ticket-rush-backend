package com.uet.ticketrush.dtos;

import java.util.List;
import java.util.UUID;

public record BookingRequestDTO (
        UUID userId,
        UUID eventId,
        List<UUID> seatIds
) {
}
