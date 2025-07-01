package com.hotelbooking.hotel_service.service;

import com.hotelbooking.common_model.BookingQueueDTO;
import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.common_model.Room;
import com.hotelbooking.common_model.RoomAvailability;
import com.hotelbooking.hotel_service.dto.HotelResponse;
import com.hotelbooking.hotel_service.repository.HotelRepository;
import com.hotelbooking.hotel_service.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel_service.repository.RoomRepository;
import com.hotelbooking.hotel_service.util.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class HotelService {
    @Value("${comment.service_url}")
    private String commentServiceUrl;

    @Value("${internal.secret.key}")
    private String internalSecretKey;

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RestTemplate restTemplate;

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository, RoomAvailabilityRepository roomAvailabilityRepository, RestTemplate restTemplate) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.restTemplate = restTemplate;
    }

    // Use separate cache names for different return types
    @Cacheable(value = "hotel-list", key = "'all-' + #discounted")
    public List<HotelResponse> findAll(boolean discounted) {
        return hotelRepository.findAll().stream()
                .map(hotel -> new HotelResponse(hotel, discounted, getHotelRating(hotel.getId()) != null ? getHotelRating(hotel.getId()) : 0.0))
                .toList();
    }

    @Cacheable(value = "hotel-single", key = "#id + '::' + T(com.hotelbooking.hotel_service.util.AuthUtils).isSignedIn()")
    public HotelResponse findById(Long id) {
        boolean discounted = AuthUtils.isSignedIn();
        return hotelRepository.findById(id)
                .map(hotel -> new HotelResponse(hotel, discounted, getHotelRating(id) != null ? getHotelRating(id) : 0.0))
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
                .map(h -> new HotelResponse(h, discounted, getHotelRating(h.getId()) != null ? getHotelRating(h.getId()) : 0.0))
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

    public Double getHotelRating(Long id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecretKey);

            if(AuthUtils.isSignedIn()) {
                headers.set("X-User-UserId", AuthUtils.getUserId().toString());
                headers.set("X-User-Role", "USER");
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = String.format("%s/api/v1/comments/get_rating?hotelId=%d",
                    commentServiceUrl, id);

            ResponseEntity<Double> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Double.class
            );

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
}