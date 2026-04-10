package com.uet.ticketrush.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "events", schema="public")
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private UUID event_id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime date;
    private String status;
    private LocalDateTime created_at;
    private LocalDateTime modified_at;
    private String bannerUrl;

}
