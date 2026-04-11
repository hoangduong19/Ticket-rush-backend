package com.uet.ticketrush.controllers;

import com.uet.ticketrush.dtos.BookingRequestDTO;
import com.uet.ticketrush.dtos.SeatHoldResponseDTO;
import com.uet.ticketrush.services.BookingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class BookingController {
    private final BookingFacade bookingFacade;

    //Cần chuyển HoldStatus về completed nếu khách hàng completed thanh toán
    @PostMapping("/reservations")
    public ResponseEntity<UUID> createReservation(@Valid @RequestBody BookingRequestDTO request) {
        UUID holdId = bookingFacade.createReservation(request);
        return ResponseEntity.ok(holdId);
    }

    @GetMapping("/holds/{holdId}")
    public ResponseEntity<SeatHoldResponseDTO> getHoldDetails(@PathVariable UUID holdId) {
        SeatHoldResponseDTO response = bookingFacade.getHoldDetails(holdId);
        return ResponseEntity.ok(response);
    }
}
