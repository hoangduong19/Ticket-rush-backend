package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.EventRequestDTO;
import com.uet.ticketrush.dtos.RowConfigDTO;
import com.uet.ticketrush.dtos.SeatingPayloadDTO;
import com.uet.ticketrush.enums.EventStatus;
import com.uet.ticketrush.enums.SeatStatus;
import com.uet.ticketrush.exceptions.TicketRushException;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.repos.EventRepository;
import com.uet.ticketrush.repos.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final CloudinaryService cloudinaryService;

    // Hàm lấy tất cả sự kiện
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện với ID: " + eventId));
    }

    public Event createEvent(EventRequestDTO dto) {
        Event event = Event.builder()
                .title(dto.title())
                .description(dto.description())
                .location(dto.location())
                .date(dto.date())
                .status(EventStatus.Published)
                .category(dto.category())
                .build();

        event.validateBasicInfo();
        return eventRepository.save(event);
    }

    // Trong EventService.java
    @Transactional
    public void generateSeatsFromConfig(UUID eventId, SeatingPayloadDTO payload) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketRushException("Sự kiện không tồn tại", HttpStatus.NOT_FOUND));

        List<Seat> allSeats = new ArrayList<>();

        // Duyệt qua từng cấu hình hàng (RowConfig) mà Frontend gửi lên
        for (RowConfigDTO rowConfig : payload.getRowConfigs()) {
            // Với mỗi hàng, tạo số lượng ghế tương ứng (seatsPerRow)
            for (int s = 1; s <= payload.getSeatsPerRow(); s++) {
                Seat seat = Seat.builder()
                        .event(event)
                        .sectionName(payload.getSectionLabel()) // Tên khu vực (VIP, Thường...)
                        .rowNumber(rowConfig.getRowNumber())    // Số hàng
                        .seatNumber(s)                          // Số ghế
                        .price(rowConfig.getPrice())            // Giá tiền của hàng đó
                        .status(SeatStatus.Available)
                        .seatType(rowConfig.getSeatType())
                        .version(0)
                        .build();
                allSeats.add(seat);
            }
        }
        seatRepository.saveAll(allSeats);
    }

    public List<Seat> getSeatsStatus(UUID eventId) {
        return seatRepository.findByEvent_EventId(eventId);
    }

    public Event createEventWithImage(EventRequestDTO dto, MultipartFile file) {
        // 1. Upload ảnh lên Cloudinary và lấy URL
        String imageUrl = cloudinaryService.uploadEventBanner(file);

        // 2. Tạo đối tượng Event
        Event event = Event.builder()
                .title(dto.title())
                .description(dto.description())
                .location(dto.location())
                .date(dto.date())
                .status(EventStatus.Published)
                .category(dto.category())
                .bannerUrl(imageUrl) // LƯU LINK ẢNH VÀO ĐÂY
                .build();

        event.validateBasicInfo();
        return eventRepository.save(event);
    }
}
