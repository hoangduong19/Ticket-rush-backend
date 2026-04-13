package com.uet.ticketrush.dtos;

import com.uet.ticketrush.models.Ticket;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TicketResponseDTO(
        UUID ticketId,
        String eventName,
        String sectionName,
        Integer rowNumber,
        Integer seatNumber,
        BigDecimal price,
        String qrCodeData,
        LocalDateTime purchaseDate,
        String bannerUrl
) {
    public static TicketResponseDTO fromEntity(Ticket t) {
        return TicketResponseDTO.builder()
                .ticketId(t.getTicketId())
                .eventName(t.getEvent().getTitle())
                .sectionName(t.getSeat().getSectionName())
                .rowNumber(t.getSeat().getRowNumber())
                .seatNumber(t.getSeat().getSeatNumber())
                .price(t.getPrice())
                .qrCodeData(t.getTicketId().toString())
                .purchaseDate(t.getPurchaseDate())
                .bannerUrl(t.getEvent().getBannerUrl())
                .build();
    }
}
