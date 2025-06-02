package com.fabish.hotel.repository;

import com.fabish.hotel.model.Room;
import com.fabish.hotel.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByIsAvailable(Boolean isAvailable);
    List<Room> findByType(RoomType type);
    List<Room> findByFloor(Integer floor);
    List<Room> findByPriceBetween(Double minPrice, Double maxPrice);
    List<Room> findByAmenitiesContaining(String amenity);
    List<Room> findByView(String view);
    List<Room> findByIsPetFriendlyTrue();
    List<Room> findByIsSmokingAllowedTrue();
    boolean existsByRoomNumber(String roomNumber);

    List<Room> findBySizeGreaterThanEqual(Double size);

    @Query("SELECT r FROM Room r WHERE r.isAvailable = true AND r.type = ?1 AND r.maxOccupancy >= ?2")
    List<Room> findAvailableRoomsByTypeAndOccupancy(RoomType type, Integer occupancy);

    @Query("SELECT r FROM Room r WHERE r.isAvailable = true AND r.price <= ?1")
    List<Room> findAvailableRoomsByMaxPrice(Double maxPrice);

    @Query("SELECT r FROM Room r WHERE r.isAvailable = true AND r.floor = ?1 AND r.type = ?2")
    List<Room> findAvailableRoomsByFloorAndType(Integer floor, RoomType type);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.isAvailable = true AND r.type = ?1")
    Long countAvailableRoomsByType(RoomType type);

    @Query("SELECT AVG(r.price) FROM Room r WHERE r.type = ?1")
    Double getAveragePriceByType(RoomType type);

    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "b.status IN ('CONFIRMED', 'CHECKED_IN') AND " +
           "((b.checkIn <= ?2 AND b.checkOut >= ?1) OR " +
           "(b.checkIn >= ?1 AND b.checkIn < ?2)))")
    List<Room> findAvailableRoomsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT r FROM Room r WHERE r.type = ?1 AND r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "b.status IN ('CONFIRMED', 'CHECKED_IN') AND " +
           "((b.checkIn <= ?3 AND b.checkOut >= ?2) OR " +
           "(b.checkIn >= ?2 AND b.checkIn < ?3)))")
    List<Room> findAvailableRoomsByTypeAndDateRange(RoomType type, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT r.* FROM rooms r " +
           "JOIN room_amenities ra ON r.id = ra.room_id " +
           "WHERE ra.amenity = ?1 AND r.id NOT IN " +
           "(SELECT b.room_id FROM bookings b WHERE " +
           "b.status IN ('CONFIRMED', 'CHECKED_IN') AND " +
           "((b.check_in <= ?3 AND b.check_out >= ?2) OR " +
           "(b.check_in >= ?2 AND b.check_in < ?3)))", 
           nativeQuery = true)
    List<Room> findAvailableRoomsByAmenityAndDateRange(String amenity, LocalDateTime startDate, LocalDateTime endDate);
}
