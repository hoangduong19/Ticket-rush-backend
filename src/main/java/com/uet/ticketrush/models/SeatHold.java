package com.uet.ticketrush.models;

import com.uet.ticketrush.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.*;
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
}