package com.fabish.hotel.controller;

import com.fabish.hotel.model.Room;
import com.fabish.hotel.model.RoomType;
import com.fabish.hotel.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(roles = {"ADMIN"})
    void getAllRooms_ShouldReturnAllRooms() throws Exception {
        when(roomService.getAllRooms()).thenReturn(Arrays.asList(room1, room2));

        mockMvc.perform(get("/rooms/api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].roomNumber", is("101")))
                .andExpect(jsonPath("$[0].type", is("SINGLE")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].roomNumber", is("201")))
                .andExpect(jsonPath("$[1].type", is("DOUBLE")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getRoomById_WithValidId_ShouldReturnRoom() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room1));

        mockMvc.perform(get("/rooms/api/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.roomNumber", is("101")))
                .andExpect(jsonPath("$.type", is("SINGLE")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getRoomById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(roomService.getRoomById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/rooms/api/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("Room not found")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createRoom_WithValidRoom_ShouldReturnCreatedRoom() throws Exception {
        Room newRoom = Room.builder()
                .roomNumber("301")
                .type(RoomType.SUITE)
                .price(300.0)
                .isAvailable(true)
                .floor(3)
                .build();

        Room createdRoom = Room.builder()
                .id(3L)
                .roomNumber("301")
                .type(RoomType.SUITE)
                .price(300.0)
                .isAvailable(true)
                .floor(3)
                .build();

        when(roomService.createRoom(any(Room.class))).thenReturn(createdRoom);

        mockMvc.perform(post("/rooms/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRoom)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.roomNumber", is("301")))
                .andExpect(jsonPath("$.type", is("SUITE")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateRoom_WithValidIdAndRoom_ShouldReturnUpdatedRoom() throws Exception {
        Room updatedRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .type(RoomType.DELUXE)
                .price(200.0)
                .isAvailable(true)
                .floor(1)
                .build();

        when(roomService.updateRoom(eq(1L), any(Room.class))).thenReturn(updatedRoom);

        mockMvc.perform(put("/rooms/api/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRoom)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.roomNumber", is("101")))
                .andExpect(jsonPath("$.type", is("DELUXE")))
                .andExpect(jsonPath("$.price", is(200.0)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteRoom_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/rooms/api/1"))
                .andExpect(status().isNoContent());
    }

//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void batchRoom_WithValidId_ShouldReturnBatchedRooms() throws Exception {
//        mockMvc.perform(get("/api/auth/me"))
//                .andExpect(status().isOk());
//    }

    @Test
    @WithMockUser(roles = {"USER"})
    void accessAdminEndpoint_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/rooms/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(room1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessProtectedEndpoint_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/rooms/api"))
                .andExpect(status().isUnauthorized());
    }
}