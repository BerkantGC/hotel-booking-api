package com.hotelbooking.hotel_admin_service.service;

import com.hotelbooking.hotel_admin_service.dto.HotelDTO;
import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.hotel_admin_service.repository.HotelRepository;
import com.hotelbooking.hotel_admin_service.util.ImageUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class HotelService {

    private final HotelRepository repository;
    private final ImageUtils imageUtils;

    public HotelService(HotelRepository repository, ImageUtils imageUtils) {
        this.repository = repository;
        this.imageUtils = imageUtils;
    }

    public List<Hotel> getAllHotels(Long adminId) {
        return repository.findAllByAdminId(adminId);
    }

    public Hotel addHotel(HotelDTO hotelDTO, Long adminId) {
        try {
            Hotel hotel = new Hotel();
            hotel.setName(hotelDTO.getName());
            hotel.setLocation(hotelDTO.getLocation());
            hotel.setDescription(hotelDTO.getDescription());
            hotel.setRoomCount(hotelDTO.getRoom_count());
            hotel.setAdminId(adminId);

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

    public Hotel updateHotel(Long id, Hotel updated) {
        Hotel hotel = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        hotel.setName(updated.getName());
        hotel.setLocation(updated.getLocation());
        hotel.setDescription(updated.getDescription());
        hotel.setRoomCount(updated.getRoomCount());
        return repository.save(hotel);
    }
}
