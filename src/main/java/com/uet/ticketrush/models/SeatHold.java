package com.uet.ticketrush.models;

import com.uet.ticketrush.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "seat_holds")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SeatHold {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hold_id", updatable = false, nullable = false)
    private UUID holdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private HoldStatus status;

    // Ánh xạ bảng trung gian seat_hold_items
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "seat_hold_items",
            joinColumns = @JoinColumn(name = "hold_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private Set<Seat> seats = new HashSet<>();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void complete() {
        this.status = HoldStatus.Completed;
    }

    public void markAsExpired() {
        this.status = HoldStatus.Expired;
    }

    public static SeatHold createPendingHold(User user, Event event, Set<Seat> seats, int expiryMinutes) {
        return SeatHold.builder()
                .user(user)
                .event(event)
                .seats(seats)
                .status(HoldStatus.Active)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();
    }

    public BigDecimal getTotalPrice() {
        return seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getSecondsLeft() {
        long seconds = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        return Math.max(0, seconds);
    }
}