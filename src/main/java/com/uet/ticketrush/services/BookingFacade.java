package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.BookingRequestDTO;
import com.uet.ticketrush.dtos.SeatHoldResponseDTO;
import com.uet.ticketrush.dtos.SeatSyncResult;
import com.uet.ticketrush.enums.HoldStatus;
import com.uet.ticketrush.exceptions.TicketRushException;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.models.SeatHold;
import com.uet.ticketrush.repos.EventRepository;
import com.uet.ticketrush.repos.SeatHoldRepository;
import com.uet.ticketrush.repos.SeatRepository;
import com.uet.ticketrush.repos.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingFacade {
    private final SeatService seatService;
    private final VirtualQueueService virtualQueueService;
    private final SeatHoldService holdService;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository holdRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    public UUID createReservation(BookingRequestDTO request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new TicketRushException("Sự kiện không tồn tại", HttpStatus.NOT_FOUND));
        event.validateBookable();

        LocalDateTime sessionExpiresAt = virtualQueueService.validateAndGetSessionExpiry(request.eventId(), request.userId());

        Optional<SeatHold> existingHoldOpt = holdRepository.findByUser_UserIdAndEvent_EventIdAndStatus(
                request.userId(), request.eventId(), HoldStatus.Active);

        if (existingHoldOpt.isEmpty()) {
            seatService.lockSeats(request.seatIds());
            return holdService.createHold(request.userId(), request.eventId(), request.seatIds(), sessionExpiresAt).getHoldId();
        }

        SeatHold existingHold = existingHoldOpt.get();

        SeatSyncResult changes = existingHold.calculateChanges(request.seatIds());


        if (!changes.hasChanges()) {
            return existingHold.getHoldId();
        }

        //Remove truoc khi add
        if (!changes.seatsToRemove().isEmpty()) {
            seatService.unlockSeats(changes.seatsToRemove());
            holdService.removeSeatsFromHold(existingHold, changes.seatsToRemove());
        }

        if (!changes.seatsToAdd().isEmpty()) {
            seatService.lockSeats(changes.seatsToAdd());
            holdService.addSeatsToExistingHold(existingHold, changes.seatsToAdd(), sessionExpiresAt);
        }

        return existingHold.getHoldId();

//        if (existingHoldOpt.isPresent()) {
//            SeatHold existingHold = existingHoldOpt.get();
//
//            List<UUID> trulyNewIds = filterNewSeats(existingHold, request.seatIds());
//
//            if (!trulyNewIds.isEmpty()) {
//                seatService.lockSeats(trulyNewIds);
//                holdService.addSeatsToExistingHold(existingHold, trulyNewIds, sessionExpiresAt);
//            }
//            return existingHoldOpt.get().getHoldId();
//        } else {
//            seatService.lockSeats(request.seatIds());
//            return holdService.createHold(request.userId(), request.eventId(), request.seatIds(), sessionExpiresAt).getHoldId();
//        }
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
