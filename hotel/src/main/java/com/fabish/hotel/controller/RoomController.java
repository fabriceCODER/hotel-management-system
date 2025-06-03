package com.fabish.hotel.controller;

import com.fabish.hotel.exception.ResourceNotFoundException;
import com.fabish.hotel.model.Room;
import com.fabish.hotel.model.RoomType;
import com.fabish.hotel.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller @RequestMapping("/rooms") @RequiredArgsConstructor public class RoomController {

    private final RoomService roomService;

    // MVC endpoints for web UI
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("roomTypes", RoomType.values());
        return "rooms/list";
    }

    @GetMapping("/available")
    public String listAvailableRooms(Model model) {
        model.addAttribute("rooms", roomService.getAvailableRooms());
        model.addAttribute("roomTypes", RoomType.values());
        return "rooms/available";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", RoomType.values());
        return "rooms/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createRoom(@Valid @ModelAttribute Room room, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roomTypes", RoomType.values());
            return "rooms/form";
        }

        try {
            roomService.createRoom(room);
            redirectAttributes.addFlashAttribute("success", "Room created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Room> roomOpt = roomService.getRoomById(id);
            if (roomOpt.isPresent()) {
                model.addAttribute("room", roomOpt.get());
                model.addAttribute("roomTypes", RoomType.values());
                return "rooms/form";
            } else {
                redirectAttributes.addFlashAttribute("error", "Room not found with id: " + id);
                return "redirect:/rooms";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/rooms";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRoom(@PathVariable Long id, @Valid @ModelAttribute Room room, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roomTypes", RoomType.values());
            return "rooms/form";
        }

        try {
            roomService.updateRoom(id, room);
            redirectAttributes.addFlashAttribute("success", "Room updated successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "Room deleted successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @GetMapping("/search")
    public String searchRooms(
            @RequestParam(required = false) RoomType type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Boolean petFriendly,
            @RequestParam(required = false) Boolean smoking,
            @RequestParam(required = false) String amenity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        List<Room> rooms;

        if (startDate != null && endDate != null) {
            if (type != null) {
                rooms = roomService.getAvailableRoomsByTypeAndDateRange(type, startDate, endDate);
            } else if (amenity != null) {
                rooms = roomService.getAvailableRoomsByAmenityAndDateRange(amenity, startDate, endDate);
            } else {
                rooms = roomService.getAvailableRoomsByDateRange(startDate, endDate);
            }
        } else {
            if (type != null) {
                rooms = roomService.getRoomsByType(type);
            } else if (minPrice != null && maxPrice != null) {
                rooms = roomService.getRoomsByPriceRange(minPrice, maxPrice);
            } else if (floor != null) {
                rooms = roomService.getRoomsByFloor(floor);
            } else if (view != null) {
                rooms = roomService.getRoomsByView(view);
            } else if (petFriendly != null && petFriendly) {
                rooms = roomService.getPetFriendlyRooms();
            } else if (smoking != null && smoking) {
                rooms = roomService.getSmokingRooms();
            } else if (amenity != null) {
                rooms = roomService.getRoomsByAmenity(amenity);
            } else {
                rooms = roomService.getAllRooms();
            }
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("roomTypes", RoomType.values());
        return "rooms/search";
    }

    @PostMapping("/{id}/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String addMaintenanceNote(
            @PathVariable Long id,
            @RequestParam String note,
            RedirectAttributes redirectAttributes) {
        try {
            roomService.addMaintenanceNote(id, note);
            redirectAttributes.addFlashAttribute("success", "Maintenance note added successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/cleaning")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String markRoomAsCleaned(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            roomService.markRoomAsCleaned(id);
            redirectAttributes.addFlashAttribute("success", "Room marked as cleaned successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    // REST API endpoints for frontend integration
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        try {
            return roomService.getRoomById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/api/available")
    @ResponseBody
    public ResponseEntity<List<Room>> getAvailableRooms() {
        return ResponseEntity.ok(roomService.getAvailableRooms());
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Room>> searchRoomsApi(
            @RequestParam(required = false) RoomType type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Boolean petFriendly,
            @RequestParam(required = false) Boolean smoking,
            @RequestParam(required = false) String amenity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Room> rooms;

        if (startDate != null && endDate != null) {
            if (type != null) {
                rooms = roomService.getAvailableRoomsByTypeAndDateRange(type, startDate, endDate);
            } else if (amenity != null) {
                rooms = roomService.getAvailableRoomsByAmenityAndDateRange(amenity, startDate, endDate);
            } else {
                rooms = roomService.getAvailableRoomsByDateRange(startDate, endDate);
            }
        } else {
            if (type != null) {
                rooms = roomService.getRoomsByType(type);
            } else if (minPrice != null && maxPrice != null) {
                rooms = roomService.getRoomsByPriceRange(minPrice, maxPrice);
            } else if (floor != null) {
                rooms = roomService.getRoomsByFloor(floor);
            } else if (view != null) {
                rooms = roomService.getRoomsByView(view);
            } else if (petFriendly != null && petFriendly) {
                rooms = roomService.getPetFriendlyRooms();
            } else if (smoking != null && smoking) {
                rooms = roomService.getSmokingRooms();
            } else if (amenity != null) {
                rooms = roomService.getRoomsByAmenity(amenity);
            } else {
                rooms = roomService.getAllRooms();
            }
        }

        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/api")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRoomApi(@Valid @RequestBody Room room) {
        try {
            Room createdRoom = roomService.createRoom(room);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRoomApi(@PathVariable Long id, @Valid @RequestBody Room room) {
        try {
            Room updatedRoom = roomService.updateRoom(id, room);
            return ResponseEntity.ok(updatedRoom);
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRoomApi(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}