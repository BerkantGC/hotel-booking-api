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
        STANDARD,
        FAMILY
    }
    
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "guest_count")
    private int guestCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    private RoomKind kind;
}
