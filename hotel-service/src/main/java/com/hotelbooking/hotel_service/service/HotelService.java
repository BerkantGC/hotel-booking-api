package com.hotelbooking.hotel_service.service;

import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.common_model.Room;
import com.hotelbooking.hotel_service.dto.HotelResponse;
import com.hotelbooking.hotel_service.repository.HotelRepository;
import com.hotelbooking.hotel_service.repository.RoomRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
    }

    // Use separate cache names for different return types
    @Cacheable(value = "hotel-list", key = "'all-' + #discounted")
    public List<HotelResponse> findAll(boolean discounted) {
        return hotelRepository.findAll().stream()
                .map(hotel -> new HotelResponse(hotel, discounted))
                .toList();
    }

    // Use different cache name for single objects
    @Cacheable(value = "hotel-single", key = "#id + '-' + #discounted")
    public HotelResponse findById(Long id, boolean discounted) {
        return hotelRepository.findById(id)
                .map(hotel -> new HotelResponse(hotel, discounted))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found!"));
    }

    @Cacheable(value = "hotel-search", key = "#location + '-' + #roomCount + '-' + #checkIn + '-' + #checkOut + '-' + #discounted",
    unless = "#result.isEmpty()")
    public List<HotelResponse> search(String location, int roomCount, LocalDate checkIn, LocalDate checkOut, boolean discounted) {
        List<Room> rooms = roomRepository.searchRooms(location, roomCount, Date.valueOf(checkIn), Date.valueOf(checkOut));
        List<Hotel> hotels = rooms.stream()
                .map(Room::getHotel)
                .distinct()
                .toList();

        return hotels.stream()
                .map(h -> new HotelResponse(h, discounted))
                .toList();
    }

    @Cacheable(value = "hotels-by-location", key = "#location")
    public List<Hotel> findByLocation(String location) {
        System.out.println("Fetching hotels by location: " + location + " from database...");
        return hotelRepository.findAllByLocationContainingIgnoreCase(location);
    }

    // Updated cache eviction methods
    @CacheEvict(value = "hotel-list", key = "'all-true'")
    public void evictAllHotelsCacheDiscounted() {
        System.out.println("Evicted all hotels cache (discounted)");
    }

    @CacheEvict(value = "hotel-list", key = "'all-false'")
    public void evictAllHotelsCacheRegular() {
        System.out.println("Evicted all hotels cache (regular)");
    }

    @CacheEvict(value = {"hotel-list", "hotel-single"}, allEntries = true)
    public void evictHotelCache(Long id) {
        System.out.println("Evicted hotel cache for ID: " + id);
    }

    @CacheEvict(value = {"hotel-list", "hotel-single", "hotel-search", "hotels-by-location"}, allEntries = true)
    public void evictAllCache() {
        System.out.println("Evicted all hotel-related caches");
    }
}