package com.hotelbooking.hotel_service.service;

import com.hotelbooking.common_model.*;
import com.hotelbooking.hotel_service.dto.HotelResponse;
import com.hotelbooking.hotel_service.dto.RoomResponse;
import com.hotelbooking.hotel_service.repository.HotelRepository;
import com.hotelbooking.hotel_service.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel_service.repository.RoomRepository;
import com.hotelbooking.hotel_service.util.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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

    // New paginated method
    @Cacheable(value = "hotel-list-paged", key = "'all-' + #discounted + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize")
    public PagedResponse<HotelResponse> findAllPaged(boolean discounted, Pageable pageable) {
        Page<Hotel> hotelPage = hotelRepository.findAll(pageable);
        List<HotelResponse> content = hotelPage.getContent().stream()
                .map(hotel -> new HotelResponse(hotel, discounted, getHotelRating(hotel.getId()) != null ? getHotelRating(hotel.getId()) : 0.0))
                .toList();
        return new PagedResponse<>(content, hotelPage.getNumber(), hotelPage.getSize(), hotelPage.getTotalElements());
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

    // Paginated search method
    @Cacheable(value = "hotel-search-paged", key = "#location + '-' + #roomCount + '-' + #checkIn + '-' + #checkOut + '-' + #discounted + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize",
    unless = "#result.content.isEmpty()")
    public PagedResponse<HotelResponse> searchPaged(String location, int guestCount, LocalDate checkIn, LocalDate checkOut, boolean discounted, Pageable pageable) {
        List<Hotel> hotels = hotelRepository.findAllByLocationContainingIgnoreCase(location);

        List<Hotel> availableHotels = hotels.stream()
                .filter(hotel -> hasAvailableRoom(hotel, guestCount, checkIn, checkOut))
                .toList();

        // Manual pagination since we need to filter first
        int totalElements = availableHotels.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), totalElements);
        
        List<Hotel> pagedHotels = availableHotels.subList(start, end);
        
        List<HotelResponse> content = pagedHotels.stream()
                .map(h -> new HotelResponse(h, discounted, getHotelRating(h.getId()) != null ? getHotelRating(h.getId()) : 0.0))
                .toList();
        
        return new PagedResponse<>(content, pageable.getPageNumber(), pageable.getPageSize(), totalElements);
    }

    public List<RoomResponse> getHotelRooms(Long id, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found!"));
        
        List<Room> rooms = hotel.getRooms();
        List<RoomResponse> roomRespons = new ArrayList<>();
        
        // If dates are provided, check availability
        if (checkIn != null && checkOut != null) {
            log.info("Checking room availability for hotel ID: " + id + " with dates: " + checkIn + " - " + checkOut);
            
            for (Room room : rooms) {
                List<RoomAvailability> availabilities = roomAvailabilityRepository
                        .findByRoomIdAndDateBetween(room.getId(), checkIn, checkOut.minusDays(1));
                
                int totalDays = checkIn.until(checkOut).getDays();
                boolean isAvailable = availabilities.size() == totalDays &&
                        availabilities.stream().allMatch(a -> a.getAvailableCount() >= 1);
                
                if (isAvailable) {
                    RoomResponse roomResponse = new RoomResponse(room, guestCount <= room.getGuestCount());
                    roomResponse.setAvailablityList(availabilities.stream().map(roomAvailability -> new RoomAvailabilityResponse(roomAvailability.getAvailableCount(), roomAvailability.getDate())).toList());
                    roomRespons.add(roomResponse);
                } else {
                    List<RoomAvailability> roomAvailabilities = roomAvailabilityRepository.findByRoomId(room.getId());
                    RoomResponse roomResponse = new RoomResponse(room, false);
                    roomResponse.setAvailablityList(roomAvailabilities.stream().map(roomAvailability ->
                            new com.hotelbooking.common_model.RoomAvailabilityResponse(roomAvailability.getAvailableCount(), roomAvailability.getDate())
                    ).toList());
                    roomRespons.add(roomResponse);
                }
            }
        } else {
            roomRespons = rooms.stream()
                    .map(room -> {
                        List<RoomAvailability> roomAvailabilities = roomAvailabilityRepository.findByRoomId(room.getId());
                        RoomResponse roomResponse = new RoomResponse(room, guestCount <= room.getGuestCount());
                        roomResponse.setAvailablityList(roomAvailabilities.stream().map(roomAvailability ->
                                new com.hotelbooking.common_model.RoomAvailabilityResponse(roomAvailability.getAvailableCount(), roomAvailability.getDate())
                        ).toList());

                        return roomResponse;
                    })
                    .toList();
        }

        return roomRespons;
    }

    // Paginated version of getHotelRooms
    public PagedResponse<RoomResponse> getHotelRoomsPaged(Long id, LocalDate checkIn, LocalDate checkOut, int guestCount, Pageable pageable) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found!"));
        
        List<Room> rooms = hotel.getRooms();
        List<RoomResponse> roomRespons = new ArrayList<>();
        
        // If dates are provided, check availability
        if (checkIn != null && checkOut != null) {
            log.info("Checking room availability for hotel ID: " + id + " with dates: " + checkIn + " - " + checkOut);
            
            for (Room room : rooms) {
                List<RoomAvailability> availabilities = roomAvailabilityRepository
                        .findByRoomIdAndDateBetween(room.getId(), checkIn, checkOut.minusDays(1));
                
                int totalDays = checkIn.until(checkOut).getDays();
                boolean isAvailable = availabilities.size() == totalDays &&
                        availabilities.stream().allMatch(a -> a.getAvailableCount() >= 1);
                
                if (isAvailable) {
                    RoomResponse roomResponse = new RoomResponse(room, guestCount <= room.getGuestCount());
                    roomResponse.setAvailablityList(availabilities.stream().map(roomAvailability -> new RoomAvailabilityResponse(roomAvailability.getAvailableCount(), roomAvailability.getDate())).toList());
                    roomRespons.add(roomResponse);
                } else {
                    List<RoomAvailability> roomAvailabilities = roomAvailabilityRepository.findByRoomId(room.getId());
                    RoomResponse roomResponse = new RoomResponse(room, false);
                    roomResponse.setAvailablityList(roomAvailabilities.stream().map(roomAvailability ->
                            new com.hotelbooking.common_model.RoomAvailabilityResponse(roomAvailability.getAvailableCount(), roomAvailability.getDate())
                    ).toList());
                    roomRespons.add(roomResponse);
                }
            }
        } else {
            roomRespons = rooms.stream()
                    .map(room -> {
                        List<RoomAvailability> roomAvailabilities = roomAvailabilityRepository.findByRoomId(room.getId());
                        RoomResponse roomResponse = new RoomResponse(room, guestCount <= room.getGuestCount());
                        roomResponse.setAvailablityList(roomAvailabilities.stream().map(roomAvailability ->
                                new com.hotelbooking.common_model.RoomAvailabilityResponse(roomAvailability.getAvailableCount(), roomAvailability.getDate())
                        ).toList());

                        return roomResponse;
                    })
                    .toList();
        }

        // Manual pagination
        int totalElements = roomRespons.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), totalElements);
        
        List<RoomResponse> pagedRooms = roomRespons.subList(start, end);
        
        return new PagedResponse<>(pagedRooms, pageable.getPageNumber(), pageable.getPageSize(), totalElements);
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

        return room.getGuestCount() >= guestCount;
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

    public void updateAvailability(Long hotelId, BookingQueueDTO request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found!"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found!"));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room does not belong to hotel");
        }
        LocalDate checkIn = request.getCheckIn();
        LocalDate checkOut = request.getCheckOut();
        int guestCount = request.getGuestCount();
        int totalDays = checkIn.until(checkOut).getDays();

        for (int i = 0; i < totalDays; i++) {
            LocalDate date = checkIn.plusDays(i);
            RoomAvailability availability = roomAvailabilityRepository
                    .findByRoomIdAndDate(request.getRoomId(), date)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability not found for date: " + date));

            if (availability.getAvailableCount() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room not available on " + date);
            }

            availability.setAvailableCount(availability.getAvailableCount() - 1);
            roomAvailabilityRepository.save(availability);
        }
    }
    @Cacheable(value = "hotels-by-location", key = "#location")
    public List<Hotel> findByLocation(String location) {
        System.out.println("Fetching hotels by location: " + location + " from database...");
        return hotelRepository.findAllByLocationContainingIgnoreCase(location);
    }

    // Updated cache eviction methods
    @CacheEvict(value = {"hotel-list", "hotel-list-paged"}, key = "'all-true'")
    public void evictAllHotelsCacheDiscounted() {
        System.out.println("Evicted all hotels cache (discounted)");
    }

    @CacheEvict(value = {"hotel-list", "hotel-list-paged"}, key = "'all-false'")
    public void evictAllHotelsCacheRegular() {
        System.out.println("Evicted all hotels cache (regular)");
    }

    @CacheEvict(value = {"hotel-list", "hotel-list-paged", "hotel-single"}, allEntries = true)
    public void evictHotelCache(Long id) {
        System.out.println("Evicted hotel cache for ID: " + id);
    }

    @CacheEvict(value = {"hotel-list", "hotel-list-paged", "hotel-single", "hotel-search", "hotel-search-paged", "hotels-by-location"}, allEntries = true)
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

    public Page<HotelResponse> findAllPaginated(Pageable pageable, boolean discounted) {
        return hotelRepository.findAll(pageable).map(hotel -> new HotelResponse(hotel, discounted, getHotelRating(hotel.getId()) != null ? getHotelRating(hotel.getId()) : 0.0));
    }

    public Page<HotelResponse> searchPaginated(String location, int guestCount, LocalDate checkIn, LocalDate checkOut, boolean discounted, Pageable pageable) {
        List<Hotel> hotels = hotelRepository.findAllByLocationContainingIgnoreCase(location);

        List<Hotel> availableHotels = hotels.stream()
                .filter(hotel -> hasAvailableRoom(hotel, guestCount, checkIn, checkOut))
                .toList();

        List<HotelResponse> hotelResponses = availableHotels.stream()
                .map(h -> new HotelResponse(h, discounted, getHotelRating(h.getId()) != null ? getHotelRating(h.getId()) : 0.0))
                .collect(Collectors.toList());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), hotelResponses.size());
        
        List<HotelResponse> pageContent = hotelResponses.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, hotelResponses.size());
    }
}