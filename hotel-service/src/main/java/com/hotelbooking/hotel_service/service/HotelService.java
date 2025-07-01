package com.hotelbooking.hotel_service.service;

import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.common_model.Room;
import com.hotelbooking.common_model.RoomAvailability;
import com.hotelbooking.hotel_service.dto.HotelResponse;
import com.hotelbooking.hotel_service.repository.HotelRepository;
import com.hotelbooking.hotel_service.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel_service.repository.RoomRepository;
import com.hotelbooking.hotel_service.util.AuthUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository, RoomAvailabilityRepository roomAvailabilityRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
    }

    // Use separate cache names for different return types
    @Cacheable(value = "hotel-list", key = "'all-' + #discounted")
    public List<HotelResponse> findAll(boolean discounted) {
        return hotelRepository.findAll().stream()
                .map(hotel -> new HotelResponse(hotel, discounted))
                .toList();
    }

    @Cacheable(value = "hotel-single", key = "#id + '::' + T(com.hotelbooking.hotel_service.util.AuthUtils).isSignedIn()")
    public HotelResponse findById(Long id) {
        boolean discounted = AuthUtils.isSignedIn();
        return hotelRepository.findById(id)
                .map(hotel -> new HotelResponse(hotel, discounted))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found!"));
    }

    @Cacheable(value = "hotel-search", key = "#location + '-' + #roomCount + '-' + #checkIn + '-' + #checkOut + '-' + #discounted",
    unless = "#result.isEmpty()")
    public List<HotelResponse> search(String location, int guestCount, LocalDate checkIn, LocalDate checkOut, boolean discounted) {
        List<Hotel> hotels = hotelRepository.findAllByLocationContainingIgnoreCase(location);

        List<Hotel> availableHotels = hotels.stream()
                .filter(hotel -> hasAvailableRoom(hotel, guestCount, checkIn, checkOut))
                .toList();

        return availableHotels.stream()
                .map(h -> new HotelResponse(h, discounted))
                .toList();
    }


    public boolean isRoomAvailable(Long hotelId, UUID roomId, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found!"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found!"));

        if (!Objects.equals(room.getHotel().getId(), hotelId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room does not belong to the specified hotel!");
        }

        List<RoomAvailability> availabilities = roomAvailabilityRepository
                .findByRoomIdAndDateBetween(roomId, checkIn, checkOut.minusDays(1));

        int totalDays = checkIn.until(checkOut).getDays();
        if (availabilities.size() < totalDays) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room availability data is incomplete.");
        }

        boolean allAvailable = availabilities.stream()
                .allMatch(a -> a.getAvailableCount() >= 1); // 1 oda yeterli

        if (!allAvailable) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is not available for one or more days.");
        }

        return true;
    }

    private boolean hasAvailableRoom(Hotel hotel, int guestCount, LocalDate checkIn, LocalDate checkOut) {
        for (Room room : hotel.getRooms()) {
            List<RoomAvailability> availabilities = roomAvailabilityRepository
                    .findByRoomIdAndDateBetween(room.getId(), checkIn, checkOut.minusDays(1));

            int totalDays = checkIn.until(checkOut).getDays();

            boolean allDaysAvailable = availabilities.size() == totalDays &&
                    availabilities.stream().allMatch(a -> a.getAvailableCount() >= 1);

            if (allDaysAvailable) return true;
        }

        return false;
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