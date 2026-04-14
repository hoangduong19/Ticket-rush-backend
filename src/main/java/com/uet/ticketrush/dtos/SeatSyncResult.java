package com.uet.ticketrush.dtos;

import java.util.List;
import java.util.UUID;

public record SeatSyncResult(
        List<UUID> seatsToAdd,
        List<UUID> seatsToRemove
) {
    public boolean hasChanges() {
        return (seatsToAdd != null && !seatsToAdd.isEmpty()) ||
                (seatsToRemove != null && !seatsToRemove.isEmpty());
    }
}
