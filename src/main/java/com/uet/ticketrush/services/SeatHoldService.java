package com.uet.ticketrush.services;

import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.models.SeatHold;
import com.uet.ticketrush.models.User;
import com.uet.ticketrush.repos.EventRepository;
import com.uet.ticketrush.repos.SeatHoldRepository;
import com.uet.ticketrush.repos.SeatRepository;
import com.uet.ticketrush.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatHoldService {
    private final SeatHoldRepository holdRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    public SeatHold createHold(UUID userId, UUID eventId, List<UUID> seatIds, LocalDateTime expiry) {
        User user = userRepository.getReferenceById(userId);
        Event event = eventRepository.getReferenceById(eventId);
        List<Seat> seats = seatRepository.findAllById(seatIds);

        SeatHold hold = SeatHold.createPendingHold(user, event, new HashSet<>(seats), expiry);
        return holdRepository.save(hold);
    }

    public void addSeatsToExistingHold(SeatHold hold, List<UUID> newSeatIds, LocalDateTime expiry) {
        List<Seat> newSeats = seatRepository.findAllById(newSeatIds);
        hold.addSeats(new HashSet<>(newSeats));
        hold.setExpiresAt(expiry);
        holdRepository.save(hold);
    }

    public void removeSeatsFromHold(SeatHold hold, List<UUID> seatsToRemove) {
        List<Seat> newSeats = seatRepository.findAllById(seatsToRemove);
        hold.removeSeats(seatsToRemove);
        holdRepository.save(hold);
    }

}
