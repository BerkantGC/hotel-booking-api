package com.hotelbooking.common_model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "room_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailability {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "available_count", nullable = false)
    private int availableCount;
}
