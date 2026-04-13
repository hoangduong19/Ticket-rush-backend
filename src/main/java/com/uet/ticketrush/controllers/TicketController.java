package com.uet.ticketrush.controllers;

import com.uet.ticketrush.dtos.TicketResponseDTO;
import com.uet.ticketrush.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets() {
        return ResponseEntity.ok(ticketService.getMyTickets());
    }
}
