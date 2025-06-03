package com.fabish.hotel.service;

import com.fabish.hotel.exception.ResourceNotFoundException;
import com.fabish.hotel.model.Room;
import com.fabish.hotel.model.RoomType;
import com.fabish.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime; import java.util.List; import java.util.Optional;

@Service @Transactional @RequiredArgsConstructor public class RoomService {

    private final RoomRepository roomRepository;

    public Room createRoom(Room room) {
        validateRoomNumber(room.getRoomNumber());
        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        if (!room.getRoomNumber().equals(roomDetails.getRoomNumber())) {
            validateRoomNumber(roomDetails.getRoomNumber());
        }

        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setType(roomDetails.getType());
        room.setPrice(roomDetails.getPrice());
        room.setDescription(roomDetails.getDescription());
        room.setFloor(roomDetails.getFloor());
        room.setAmenities(roomDetails.getAmenities());
        room.setView(roomDetails.getView());
        room.setIsSmokingAllowed(roomDetails.getIsSmokingAllowed());
        room.setIsPetFriendly(roomDetails.getIsPetFriendly());
        room.setMaxOccupancy(roomDetails.getMaxOccupancy());
        room.setSize(roomDetails.getSize());
        room.setMaintenanceNotes(roomDetails.getMaintenanceNotes());

        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        roomRepository.delete(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByIsAvailable(true);
    }

    public List<Room> getRoomsByType(RoomType type) {
        return roomRepository.findByType(type);
    }

    public List<Room> getRoomsByFloor(Integer floor) {
        return roomRepository.findByFloor(floor);
    }

    public List<Room> getRoomsByPriceRange(Double minPrice, Double maxPrice) {
        return roomRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Room> getRoomsByAmenity(String amenity) {
        return roomRepository.findByAmenitiesContaining(amenity);
    }

    public List<Room> getRoomsByView(String view) {
        return roomRepository.findByView(view);
    }

    public List<Room> getPetFriendlyRooms() {
        return roomRepository.findByIsPetFriendlyTrue();
    }

    public List<Room> getSmokingRooms() {
        return roomRepository.findByIsSmokingAllowedTrue();
    }

    public void updateRoomAvailability(Long id, boolean isAvailable) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setIsAvailable(isAvailable);
        roomRepository.save(room);
    }

    public void updateRoomCleaningStatus(Long id, LocalDateTime cleaningTime) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setLastCleaned(cleaningTime);
        roomRepository.save(room);
    }

    public void markRoomAsCleaned(Long id) {
        updateRoomCleaningStatus(id, LocalDateTime.now());
    }

    public void addMaintenanceNote(Long id, String note) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        String currentNotes = room.getMaintenanceNotes();
        String newNote = LocalDate.now() + ": " + note;
        room.setMaintenanceNotes(currentNotes == null ? newNote : currentNotes + "\n" + newNote);
        roomRepository.save(room);
    }

    private void validateRoomNumber(String roomNumber) {
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new IllegalArgumentException("Room number already exists: " + roomNumber);
        }
    }

    public List<Room> getAvailableRoomsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return roomRepository.findAvailableRoomsByDateRange(startDateTime, endDateTime);
    }

    public List<Room> getAvailableRoomsByTypeAndDateRange(RoomType type, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return roomRepository.findAvailableRoomsByTypeAndDateRange(type, startDateTime, endDateTime);
    }

    public List<Room> getAvailableRoomsByAmenityAndDateRange(String amenity, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return roomRepository.findAvailableRoomsByAmenityAndDateRange(amenity, startDateTime, endDateTime);
    }

}