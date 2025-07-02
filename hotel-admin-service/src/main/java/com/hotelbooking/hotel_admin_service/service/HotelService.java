package com.hotelbooking.hotel_admin_service.service;

import com.hotelbooking.hotel_admin_service.dto.HotelDTO;
import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.hotel_admin_service.dto.HotelResponse;
import com.hotelbooking.hotel_admin_service.repository.HotelRepository;
import com.hotelbooking.hotel_admin_service.util.ImageUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Service
public class HotelService {

    private final HotelRepository repository;
    private final ImageUtils imageUtils;

    public HotelService(HotelRepository repository, ImageUtils imageUtils) {
        this.repository = repository;
        this.imageUtils = imageUtils;
    }

public List<HotelResponse> getAllHotels(Long adminId) {
    List<Hotel> hotels = repository.findAllByAdminId(adminId);
    List<HotelResponse> hotelDTOs = new ArrayList<>();

    for (Hotel hotel : hotels) {
        HotelResponse dto = new HotelResponse();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setLocation(hotel.getLocation());
        dto.setDescription(hotel.getDescription());
        dto.setRoom_count(hotel.getRoomCount());
        dto.setLatitude(hotel.getLatitude());
        dto.setLongitude(hotel.getLongitude());
        dto.setBasePrice(hotel.getBasePrice());
        dto.setImage(hotel.getImage());
        hotelDTOs.add(dto);
    }

    return hotelDTOs;
}
    

    public Hotel addHotel(HotelDTO hotelDTO, Long adminId) {
        try {
            Hotel hotel = new Hotel();
            hotel.setName(hotelDTO.getName());
            hotel.setLocation(hotelDTO.getLocation());
            hotel.setDescription(hotelDTO.getDescription());
            hotel.setRoomCount(hotelDTO.getRoom_count());
            hotel.setAdminId(adminId);
            hotel.setLatitude(hotelDTO.getLatitude());
            hotel.setLongitude(hotelDTO.getLongitude());
            hotel.setBasePrice(hotelDTO.getBasePrice());

            String imageURL = imageUtils.saveImageFile(hotelDTO.getImage(), "_hotel_admin_id_" + adminId);
            hotel.setImage(imageURL);

            return repository.save(hotel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteHotel(Long id) {
        repository.deleteById(id);
    }

    public Hotel updateHotel(Long id, HotelDTO updated) {
        Hotel hotel = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        hotel.setName(updated.getName());
        hotel.setLocation(updated.getLocation());
        hotel.setDescription(updated.getDescription());
        hotel.setRoomCount(updated.getRoom_count());
        return repository.save(hotel);
    }
}