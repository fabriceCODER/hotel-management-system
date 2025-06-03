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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    /**
     * Creates a new room after validating the room number.
     *
     * @param room The room to create.
     * @return The saved room entity.
     * @throws IllegalArgumentException if room number already exists.
     */
    public Room createRoom(Room room) {
        validateRoomNumber(room.getRoomNumber());
        return roomRepository.save(room);
    }

    /**
     * Updates an existing room with new details.
     *
     * @param id          The ID of the room to update.
     * @param roomDetails The updated room details.
     * @return The updated room entity.
     * @throws ResourceNotFoundException if room ID is not found.
     * @throws IllegalArgumentException  if new room number already exists.
     */
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
        room.setSmokingAllowed(roomDetails.isSmokingAllowed());
        room.setPetFriendly(roomDetails.isPetFriendly());
        room.setMaxOccupancy(roomDetails.getMaxOccupancy());
        room.setSize(roomDetails.getSize());
        room.setMaintenanceNotes(roomDetails.getMaintenanceNotes());

        return roomRepository.save(room);
    }

    /**
     * Deletes a room by ID.
     *
     * @param id The ID of the room to delete.
     * @throws ResourceNotFoundException if room ID is not found.
     */
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        roomRepository.delete(room);
    }

    /**
     * Retrieves all rooms.
     *
     * @return List of all rooms.
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Retrieves a room by ID.
     *
     * @param id The ID of the room.
     * @return Optional containing the room, or empty if not found.
     */
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    /**
     * Retrieves all available rooms.
     *
     * @return List of available rooms.
     */
    public List<Room> getAvailableRooms() {
        return roomRepository.findByIsAvailable(true);
    }

    /**
     * Retrieves rooms by room type.
     *
     * @param type The room type.
     * @return List of rooms with the specified type.
     */
    public List<Room> getRoomsByType(RoomType type) {
        return roomRepository.findByType(type);
    }

    /**
     * Retrieves rooms by floor.
     *
     * @param floor The floor number.
     * @return List of rooms on the specified floor.
     */
    public List<Room> getRoomsByFloor(Integer floor) {
        return roomRepository.findByFloor(floor);
    }

    /**
     * Retrieves rooms within a price range.
     *
     * @param minPrice The minimum price.
     * @param maxPrice The maximum price.
     * @return List of rooms within the price range.
     */
    public List<Room> getRoomsByPriceRange(Double minPrice, Double maxPrice) {
        return roomRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Retrieves rooms with a specific amenity.
     *
     * @param amenity The amenity to search for.
     * @return List of rooms with the specified amenity.
     */
    public List<Room> getRoomsByAmenity(String amenity) {
        return roomRepository.findByAmenitiesContaining(amenity);
    }

    /**
     * Retrieves rooms with a specific view.
     *
     * @param view The view to search for.
     * @return List of rooms with the specified view.
     */
    public List<Room> getRoomsByView(String view) {
        return roomRepository.findByView(view);
    }

    /**
     * Retrieves all pet-friendly rooms.
     *
     * @return List of pet-friendly rooms.
     */
    public List<Room> getPetFriendlyRooms() {
        return roomRepository.findByIsPetFriendlyTrue();
    }

    /**
     * Retrieves all smoking-allowed rooms.
     *
     * @return List of smoking-allowed rooms.
     */
    public List<Room> getSmokingRooms() {
        return roomRepository.findByIsSmokingAllowedTrue();
    }

    /**
     * Updates the availability status of a room.
     *
     * @param id          The ID of the room.
     * @param isAvailable The availability status.
     * @throws ResourceNotFoundException if room ID is not found.
     */
    public void updateRoomAvailability(Long id, boolean isAvailable) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setAvailable(isAvailable);
        roomRepository.save(room);
    }

    /**
     * Updates the cleaning status of a room.
     *
     * @param id           The ID of the room.
     * @param cleaningTime The cleaning timestamp.
     * @throws ResourceNotFoundException if room ID is not found.
     */
    public void updateRoomCleaningStatus(Long id, LocalDateTime cleaningTime) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setLastCleaned(cleaningTime);
        roomRepository.save(room);
    }

    /**
     * Marks a room as cleaned with the current timestamp.
     *
     * @param id The ID of the room.
     * @throws ResourceNotFoundException if room ID is not found.
     */
    public void markRoomAsCleaned(Long id) {
        updateRoomCleaningStatus(id, LocalDateTime.now());
    }

    /**
     * Adds a maintenance note to a room.
     *
     * @param id   The ID of the room.
     * @param note The maintenance note.
     * @throws ResourceNotFoundException if room ID is not found.
     */
    public void addMaintenanceNote(Long id, String note) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        String currentNotes = room.getMaintenanceNotes();
        String newNote = LocalDate.now() + ": " + note;
        room.setMaintenanceNotes(currentNotes == null ? newNote : currentNotes + "\n" + newNote);
        roomRepository.save(room);
    }

    /**
     * Validates that a room number is unique.
     *
     * @param roomNumber The room number to validate.
     * @throws IllegalArgumentException if room number already exists.
     */
    private void validateRoomNumber(String roomNumber) {
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new IllegalArgumentException("Room number already exists: " + roomNumber);
        }
    }

    /**
     * Retrieves available rooms within a date range.
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return List of available rooms.
     */
    public List<Room> getAvailableRoomsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return roomRepository.findAvailableRoomsByDateRange(startDateTime, endDateTime);
    }

    /**
     * Retrieves available rooms by type and date range.
     *
     * @param type      The room type.
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return List of available rooms.
     */
    public List<Room> getAvailableRoomsByTypeAndDateRange(RoomType type, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return roomRepository.findAvailableRoomsByTypeAndDateRange(type, startDateTime, endDateTime);
    }

    /**
     * Retrieves available rooms by amenity and date range.
     *
     * @param amenity   The amenity to search for.
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return List of available rooms.
     */
    public List<Room> getAvailableRoomsByAmenityAndDateRange(String amenity, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return roomRepository.findAvailableRoomsByAmenityAndDateRange(amenity, startDateTime, endDateTime);
    }
}