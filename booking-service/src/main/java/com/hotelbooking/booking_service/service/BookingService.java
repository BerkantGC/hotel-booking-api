package com.hotelbooking.booking_service.service;

import com.hotelbooking.booking_service.config.RabbitMQConfig;
import com.hotelbooking.booking_service.dto.BookingRequest;
import com.hotelbooking.booking_service.model.Booking;
import com.hotelbooking.booking_service.repository.BookingRepository;
import com.hotelbooking.booking_service.util.AuthUtils;
import com.hotelbooking.common_model.BookingQueueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${hotel.service.url}")
    private String hotelServiceUrl;

    @Value("${internal.secret.key}")
    private String internalSecretKey;

    public BookingService(BookingRepository bookingRepository, RabbitTemplate rabbitTemplate, RestTemplate restTemplate) {
        this.bookingRepository = bookingRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = restTemplate;
    }

    public Booking createBooking(BookingRequest request, Long userId) {
        if (!isRoomAvailable(request.getHotelId(), request.getRoomId(),
                request.getCheckIn(), request.getCheckOut(), request.getGuestCount())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available for the selected dates");
        }

        log.info("Creating booking for user {}: {}", userId, request);

        Booking booking = new Booking();
        booking.setHotelId(request.getHotelId());
        booking.setRoomId(request.getRoomId());
        booking.setStartDate(Date.valueOf(request.getCheckIn()));
        booking.setEndDate(Date.valueOf(request.getCheckOut()));
        booking.setGuestCount(request.getGuestCount());
        booking.setUserId(userId);

        booking = bookingRepository.save(booking);

        // Send event to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVATION_QUEUE, getBookingQueueDTO(booking));

        return booking;
    }

    public BookingQueueDTO showBooking(Long bookingId, Long userId) {
        log.info("Showing booking for user {}: {}", userId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Booking not found");
        }

        return getBookingQueueDTO(booking);
    }

    private boolean isRoomAvailable(Long hotelId, UUID roomId, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecretKey);
            headers.set("X-User-UserId", AuthUtils.getUserId().toString());
            headers.set("X-User-Role", AuthUtils.getUserRole()[0]);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = String.format("%s/api/v1/hotels/%d/rooms/%s/availability?checkIn=%s&checkOut=%s&guestCount=%d",
                    hotelServiceUrl, hotelId, roomId, checkIn, checkOut, guestCount);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );

            log.info("Room availability check response: {}", response.getStatusCode());
            log.info("Room availability check response body: {}", response.getBody());
            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            System.err.println("Error checking room availability: " + e.getMessage());
            return false;
        }
    }

    private BookingQueueDTO getBookingQueueDTO(Booking booking){
        BookingQueueDTO dto = new BookingQueueDTO();

        dto.setId(booking.getId());
        dto.setHotelId(booking.getHotelId());
        dto.setGuestCount(booking.getGuestCount());
        dto.setCheckIn(booking.getStartDate().toLocalDate());
        dto.setCheckOut(booking.getEndDate().toLocalDate());
        dto.setRoomId(booking.getRoomId());

        return dto;
    }
}