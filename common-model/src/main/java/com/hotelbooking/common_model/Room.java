package com.hotelbooking.common_model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Data
public class Room {
    public enum RoomKind {
        STANDART,
        FAMILY
    }
    
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    private RoomKind kind;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "available_count", nullable = false)
    private int availableCount;
}
