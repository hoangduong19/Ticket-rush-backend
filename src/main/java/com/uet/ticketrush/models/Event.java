package com.uet.ticketrush.models;

import com.uet.ticketrush.enums.EventCategory;
import com.uet.ticketrush.enums.EventStatus;
import com.uet.ticketrush.exceptions.TicketRushException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", updatable = false, nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "banner_url")
    private String bannerUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private EventCategory category;

    public void validateBasicInfo() {
        if (this.title == null || this.title.isBlank()) {
            throw new TicketRushException("Tên sự kiện là bắt buộc", HttpStatus.BAD_REQUEST); //400
        }
        if (this.date != null && this.date.isBefore(LocalDateTime.now())) {
            throw new TicketRushException("Ngày diễn ra không được ở quá khứ", HttpStatus.BAD_REQUEST); //400
        }
        if (this.location == null || this.location.isBlank()) {
            throw new TicketRushException("Địa điểm không được để trống", HttpStatus.BAD_REQUEST); //400
        }

        if (this.date.isBefore(LocalDateTime.now())) {
            throw new TicketRushException("Ngày diễn ra không được ở quá khứ", HttpStatus.BAD_REQUEST);
        }
    }

    public void validateBookable() {
        if (this.status == EventStatus.Ended || this.status == EventStatus.Cancelled) {
            throw new TicketRushException("Sự kiện đã kết thúc hoặc bị hủy", HttpStatus.BAD_REQUEST);
        }
    }
}