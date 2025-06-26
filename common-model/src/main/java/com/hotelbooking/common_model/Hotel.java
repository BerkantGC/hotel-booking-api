package com.hotelbooking.common_model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String image;
    private String location;
    private String description;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "star_rating")
    private Double starRating;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "room_count", nullable = false)
    private Integer roomCount;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    private List<Room> rooms;
}
