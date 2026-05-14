package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.EventFilterDTO;
import com.uet.ticketrush.dtos.EventRequestDTO;
import com.uet.ticketrush.dtos.RowConfigDTO;
import com.uet.ticketrush.dtos.SeatingPayloadDTO;
import com.uet.ticketrush.enums.EventStatus;
import com.uet.ticketrush.enums.SeatStatus;
import com.uet.ticketrush.exceptions.TicketRushException;
import com.uet.ticketrush.models.Event;
import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.repos.EventRepository;
import com.uet.ticketrush.repos.SeatHoldRepository;
import com.uet.ticketrush.repos.SeatRepository;
import com.uet.ticketrush.repos.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final CloudinaryService cloudinaryService;
    private final SeatHoldRepository seatHoldRepository;
    private final TicketRepository ticketRepository;

    // Hàm lấy tất cả sự kiện
    /*public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }*/

    public Page<Event> getFilteredEvents(EventFilterDTO filter) {
        Pageable pageable = PageRequest.of(
                filter.page(),
                filter.size(),
                Sort.by("date").ascending()
        );

        Specification<Event> spec = (root, query, cb) -> cb.conjunction();

        /*spec = spec.and((root, q, cb) ->
                cb.equal(root.get("status"), EventStatus.Published)
        );*/

        if (filter.category() != null && !filter.category().isEmpty())
            spec = spec.and((root, q, cb) -> root.get("category").in(filter.category()));
        if (filter.dateFrom() != null)
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("date"), filter.dateFrom()));
        if (filter.dateTo() != null)
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("date"), filter.dateTo()));
        if (filter.priceMin() != null)
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("price"), filter.priceMin()));
        if (filter.priceMax() != null)
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("price"), filter.priceMax()));
        if (filter.status() != null)
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("status"), filter.status())
            );
        return eventRepository.findAll(spec, pageable);
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
                .status(EventStatus.Draft)
                .category(dto.category())
                .build();

        event.validateBasicInfo();
        return eventRepository.save(event);
    }

    @Transactional
    public void publishEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketRushException("Sự kiện không tồn tại", HttpStatus.NOT_FOUND));

        event.setStatus(EventStatus.Published);
        eventRepository.save(event);
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

        BigDecimal minPrice = payload.getRowConfigs().stream()
                .filter(r -> "GENERAL".equalsIgnoreCase(r.getSeatType()))
                .map(RowConfigDTO::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);


        event.setPrice(minPrice);
        eventRepository.save(event);
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
                .status(EventStatus.Draft)
                .category(dto.category())
                .bannerUrl(imageUrl) // LƯU LINK ẢNH VÀO ĐÂY
                .build();

        event.validateBasicInfo();
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        // Xóa theo mảng ID giúp giảm số lượng lệnh gửi đi
        ticketRepository.deleteByEventId(eventId);
        seatHoldRepository.bulkDeleteByEventId(eventId);
        seatRepository.bulkDeleteByEventId(eventId);
        eventRepository.deleteById(eventId);
    }

    @Transactional
    public Event updateEvent(UUID id, EventRequestDTO dto, MultipartFile file, SeatingPayloadDTO seatingPayload) {
        // 1. Tìm sự kiện cũ
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new TicketRushException("Sự kiện không tồn tại", HttpStatus.NOT_FOUND));

        // 2. Kiểm tra an toàn: Nếu đã bán vé thì không cho phép sửa sơ đồ ghế (Tránh mất dữ liệu khách hàng)
        // Lưu ý: Bạn cần thêm hàm này vào TicketRepository
        boolean hasTickets = ticketRepository.existsBySeat_Event_EventId(id);
        if (hasTickets) {
            throw new TicketRushException("Không thể sửa sơ đồ ghế vì đã có vé được bán ra!", HttpStatus.BAD_REQUEST);
        }

        // 3. Cập nhật thông tin cơ bản
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setLocation(dto.location());
        event.setDate(dto.date());
        event.setCategory(dto.category());

        // 4. Xử lý ảnh (Giữ nguyên logic cũ của bạn)
        if (file != null && !file.isEmpty()) {
            if (event.getBannerUrl() != null) {
                cloudinaryService.deleteImage(event.getBannerUrl());
            }
            String newUrl = cloudinaryService.uploadEventBanner(file);
            event.setBannerUrl(newUrl);
        }

        // 5. XỬ LÝ SƠ ĐỒ GHẾ (RE-GENERATE)
        // Xóa toàn bộ ghế cũ của sự kiện này trong Database
        seatRepository.deleteByEvent_EventId(id);

        seatRepository.flush();

        List<Seat> newSeats = new ArrayList<>();
        // Chạy vòng lặp tạo ma trận ghế mới từ seatingPayload gửi lên
        for (RowConfigDTO rowConfig : seatingPayload.getRowConfigs()) {
            for (int s = 1; s <= seatingPayload.getSeatsPerRow(); s++) {
                Seat seat = Seat.builder()
                        .event(event)
                        .sectionName(seatingPayload.getSectionLabel())
                        .rowNumber(rowConfig.getRowNumber())
                        .seatNumber(s)
                        .price(rowConfig.getPrice())
                        .status(SeatStatus.Available)
                        .seatType(rowConfig.getSeatType())
                        .version(0)
                        .build();
                newSeats.add(seat);
            }
        }
        // Lưu hàng loạt ghế mới
        seatRepository.saveAll(newSeats);

        // 6. Cập nhật lại giá tiền hiển thị (Min Price) cho Event
        // Lấy giá thấp nhất trong số các hàng vừa cấu hình
        BigDecimal minPrice = seatingPayload.getRowConfigs().stream()
                .map(RowConfigDTO::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        event.setPrice(minPrice);

        event.validateBasicInfo();
        return eventRepository.save(event);
    }

    // Trong EventService.java
    public List<Event> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        for (Event event : events) {
            try {
                // Lấy giá thấp nhất
                BigDecimal minPrice = seatRepository.findMinPriceByEventId(event.getEventId(), "GENERAL");

                // QUAN TRỌNG: Nếu là Draft chưa có ghế, minPrice sẽ là null
                // Hãy gán giá trị mặc định là 0 thay vì để null gây lỗi
                event.setPrice(minPrice != null ? minPrice : BigDecimal.ZERO);
            } catch (Exception e) {
                event.setPrice(BigDecimal.ZERO);
            }
        }
        return events;
    }

}
