package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.CheckoutSuccessEvent;
import com.uet.ticketrush.dtos.TicketResponseDTO;
import com.uet.ticketrush.enums.TicketStatus;
import com.uet.ticketrush.models.SeatHold;
import com.uet.ticketrush.models.Ticket;
import com.uet.ticketrush.models.User;
import com.uet.ticketrush.repos.TicketRepository;
import com.uet.ticketrush.repos.UserRepository;
import com.uet.ticketrush.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTicketCreation(CheckoutSuccessEvent event) {
        SeatHold hold = event.hold();

        List<Ticket> tickets = event.seats().stream().map(seat ->
                Ticket.builder()
                        .user(hold.getUser())
                        .event(hold.getEvent())
                        .seat(seat)
                        .price(seat.getPrice())
                        .status(TicketStatus.Sold)
                        .build()
        ).toList();

        ticketRepository.saveAll(tickets);
    }

    public List<TicketResponseDTO> getMyTickets() {
        String currentUsername = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(currentUsername);

        List<TicketResponseDTO> tickets = ticketRepository.findByUser_UserIdOrderByPurchaseDateDesc(user.getUserId())
                .stream()
                .map(TicketResponseDTO::fromEntity)
                .toList();
        return tickets;
    }
}
