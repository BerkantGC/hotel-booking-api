package com.hotelbooking.hotel_admin_service.service;

import com.hotelbooking.common_model.*;
import com.hotelbooking.hotel_admin_service.dto.RoomDTO;
import com.hotelbooking.hotel_admin_service.repository.HotelRepository;
import com.hotelbooking.hotel_admin_service.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel_admin_service.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    public RoomService(HotelRepository hotelRepository, RoomRepository roomRepository, RoomAvailabilityRepository roomAvailabilityRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
    }

    public void createRoom(RoomDTO roomDTO) {
        Hotel hotel = hotelRepository.findById(roomDTO.getHotelId())
                .orElseThrow(()-> new RuntimeException("Hotel not found"));

        Optional<Room> optionalRoom = roomRepository.findByHotelIdAndKind(roomDTO.getHotelId(), roomDTO.getKind());
        Room room;

        if (optionalRoom.isPresent()) {
            room = optionalRoom.get();
        } else {
            room = new Room();
            room.setKind(roomDTO.getKind());
            room.setHotel(hotel);
            room = roomRepository.save(room);
        }

        LocalDate start = roomDTO.getStartDate();
        LocalDate end = roomDTO.getEndDate();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Optional<RoomAvailability> roomAvailabilityOptional = roomAvailabilityRepository
                    .findByRoomIdAndDate(room.getId(), date);

            RoomAvailability roomAvailability;
            if (roomAvailabilityOptional.isPresent()) {
                roomAvailability = roomAvailabilityOptional.get();
                roomAvailability.setAvailableCount(roomDTO.getCapacity());
                roomAvailabilityRepository.save(roomAvailability);
            } else {
                roomAvailability = RoomAvailability.builder()
                        .room(room)
                        .date(date)
                        .availableCount(roomDTO.getCapacity())
                        .build();

                roomAvailabilityRepository.save(roomAvailability);
            }
        }
    }

    public List<RoomResponse> getRooms(){
        List<Room> rooms = roomRepository.findAll();
        List<RoomResponse> roomResponseList = new ArrayList<>();

        rooms.forEach(room -> {
            List<RoomAvailability> roomAvailabilities = roomAvailabilityRepository.findByRoomId(room.getId());

            RoomResponse roomResponse = new RoomResponse();
            roomResponse.setId(room.getId());
            roomResponse.setKind(room.getKind());
            roomResponse.setHotel_id(room.getHotel().getId());
            roomResponse.setAvailablityList(roomAvailabilities.stream().map(roomAvailability ->
                    new RoomAvailabilityResponse(roomAvailability.getAvailableCount(), roomAvailability.getDate())
            ).toList());

            roomResponseList.add(roomResponse);
        });

        return roomResponseList;
    }
}
