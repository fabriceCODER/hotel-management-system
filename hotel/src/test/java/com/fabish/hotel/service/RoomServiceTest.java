package com.fabish.hotel.service;

import com.fabish.hotel.exception.ResourceNotFoundException;
import com.fabish.hotel.model.Room;
import com.fabish.hotel.model.RoomType;
import com.fabish.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        // Create test rooms
        room1 = Room.builder()
                .id(1L)
                .roomNumber("101")
                .type(RoomType.SINGLE)
                .price(100.0)
                .isAvailable(true)
                .floor(1)
                .build();

        room2 = Room.builder()
                .id(2L)
                .roomNumber("201")
                .type(RoomType.DOUBLE)
                .price(150.0)
                .isAvailable(true)
                .floor(2)
                .build();
    }

    @Test
    void getAllRooms_ShouldReturnAllRooms() {
        // Arrange
        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2));

        // Act
        List<Room> result = roomService.getAllRooms();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(room1));
        assertTrue(result.contains(room2));
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void getRoomById_WithValidId_ShouldReturnRoom() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));

        // Act
        Optional<Room> result = roomService.getRoomById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(room1, result.get());
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void getRoomById_WithInvalidId_ShouldReturnEmptyOptional() {
        // Arrange
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Room> result = roomService.getRoomById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(roomRepository, times(1)).findById(99L);
    }

    @Test
    void createRoom_WithValidRoom_ShouldReturnCreatedRoom() {
        // Arrange
        Room newRoom = Room.builder()
                .roomNumber("301")
                .type(RoomType.SUITE)
                .price(300.0)
                .isAvailable(true)
                .floor(3)
                .build();

        when(roomRepository.existsByRoomNumber("301")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(newRoom);

        // Act
        Room result = roomService.createRoom(newRoom);

        // Assert
        assertEquals(newRoom, result);
        verify(roomRepository, times(1)).existsByRoomNumber("301");
        verify(roomRepository, times(1)).save(newRoom);
    }

    @Test
    void createRoom_WithDuplicateRoomNumber_ShouldThrowException() {
        // Arrange
        Room newRoom = Room.builder()
                .roomNumber("101")
                .type(RoomType.SUITE)
                .price(300.0)
                .isAvailable(true)
                .floor(3)
                .build();

        when(roomRepository.existsByRoomNumber("101")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomService.createRoom(newRoom);
        });
        
        assertEquals("Room number already exists: 101", exception.getMessage());
        verify(roomRepository, times(1)).existsByRoomNumber("101");
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void updateRoom_WithValidIdAndRoom_ShouldReturnUpdatedRoom() {
        // Arrange
        Room updatedRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .type(RoomType.DELUXE)
                .price(200.0)
                .isAvailable(true)
                .floor(1)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);

        // Act
        Room result = roomService.updateRoom(1L, updatedRoom);

        // Assert
        assertEquals(updatedRoom, result);
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void updateRoom_WithInvalidId_ShouldThrowException() {
        // Arrange
        Room updatedRoom = Room.builder()
                .id(99L)
                .roomNumber("101")
                .type(RoomType.DELUXE)
                .price(200.0)
                .isAvailable(true)
                .floor(1)
                .build();

        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            roomService.updateRoom(99L, updatedRoom);
        });
        
        verify(roomRepository, times(1)).findById(99L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void deleteRoom_WithValidId_ShouldDeleteRoom() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        doNothing().when(roomRepository).delete(room1);

        // Act
        roomService.deleteRoom(1L);

        // Assert
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).delete(room1);
    }

    @Test
    void deleteRoom_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            roomService.deleteRoom(99L);
        });
        
        verify(roomRepository, times(1)).findById(99L);
        verify(roomRepository, never()).delete(any(Room.class));
    }

    @Test
    void getAvailableRooms_ShouldReturnAvailableRooms() {
        // Arrange
        when(roomRepository.findByIsAvailable(true)).thenReturn(Arrays.asList(room1, room2));

        // Act
        List<Room> result = roomService.getAvailableRooms();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(room1));
        assertTrue(result.contains(room2));
        verify(roomRepository, times(1)).findByIsAvailable(true);
    }

    @Test
    void getRoomsByType_ShouldReturnRoomsOfSpecifiedType() {
        // Arrange
        when(roomRepository.findByType(RoomType.SINGLE)).thenReturn(Arrays.asList(room1));

        // Act
        List<Room> result = roomService.getRoomsByType(RoomType.SINGLE);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(room1));
        verify(roomRepository, times(1)).findByType(RoomType.SINGLE);
    }

    @Test
    void getAvailableRoomsByDateRange_ShouldReturnAvailableRooms() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(3);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        when(roomRepository.findAvailableRoomsByDateRange(startDateTime, endDateTime))
                .thenReturn(Arrays.asList(room1, room2));

        // Act
        List<Room> result = roomService.getAvailableRoomsByDateRange(startDate, endDate);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(room1));
        assertTrue(result.contains(room2));
        verify(roomRepository, times(1)).findAvailableRoomsByDateRange(startDateTime, endDateTime);
    }

    @Test
    void markRoomAsCleaned_ShouldUpdateCleaningStatus() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.save(any(Room.class))).thenReturn(room1);

        // Act
        roomService.markRoomAsCleaned(1L);

        // Assert
        assertNotNull(room1.getLastCleaned());
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(room1);
    }

    @Test
    void addMaintenanceNote_ShouldAddNoteToRoom() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.save(any(Room.class))).thenReturn(room1);

        // Act
        roomService.addMaintenanceNote(1L, "Test maintenance note");

        // Assert
        assertNotNull(room1.getMaintenanceNotes());
        assertTrue(room1.getMaintenanceNotes().contains("Test maintenance note"));
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(room1);
    }
}