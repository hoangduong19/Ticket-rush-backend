package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.BookingRequestDTO;
import com.uet.ticketrush.dtos.SeatHoldResponseDTO;
import com.uet.ticketrush.exceptions.TicketRushException;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.models.SeatHold;
import com.uet.ticketrush.models.User;
import com.uet.ticketrush.repos.EventRepository;
import com.uet.ticketrush.repos.SeatHoldRepository;
import com.uet.ticketrush.repos.SeatRepository;
import com.uet.ticketrush.repos.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingFacade {
    private final SeatService seatService;
    private final VirtualQueueService virtualQueueService;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository holdRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    public UUID createReservation(BookingRequestDTO request) {
        LocalDateTime sessionExpiresAt = virtualQueueService.validateAndGetSessionExpiry(request.eventId(), request.userId());

        seatService.lockSeats(request.seatIds());

        User user = userRepository.getReferenceById(request.userId());
        Event event = eventRepository.getReferenceById(request.eventId());
        List<Seat> seats = seatRepository.findAllById(request.seatIds());

        SeatHold hold = SeatHold.createPendingHold(user, event, new HashSet<>(seats), sessionExpiresAt);

        return holdRepository.save(hold).getHoldId();
    }

    public SeatHoldResponseDTO getHoldDetails(UUID holdId) {
        SeatHold hold = holdRepository.findByIdWithSeats(holdId)
                .orElseThrow(() -> new TicketRushException("Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND));

        List<SeatHoldResponseDTO.SeatDTO> seatDTOs = hold.getSeats().stream()
                .map(s -> new SeatHoldResponseDTO.SeatDTO(
                        s.getSeatNumber(),
                        s.getPrice(),
                        s.getSectionName()))
                .toList();


        return new SeatHoldResponseDTO(
                hold.getHoldId(),
                hold.getExpiresAt(),
                Math.max(0, hold.getSecondsLeft()),
                seatDTOs,
                hold.getTotalPrice()
        );
    }
}
