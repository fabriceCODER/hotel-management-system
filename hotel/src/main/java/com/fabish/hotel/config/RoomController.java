package com.fabish.hotel.config;

import com.fabish.hotel.model.Room;
import com.fabish.hotel.model.RoomType;
import com.fabish.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

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
    public String createRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        try {
            roomService.createRoom(room);
            redirectAttributes.addFlashAttribute("success", "Room created successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        roomService.getRoomById(id).ifPresent(room -> model.addAttribute("room", room));
        model.addAttribute("roomTypes", RoomType.values());
        return "rooms/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRoom(@PathVariable Long id, @ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        try {
            roomService.updateRoom(id, room);
            redirectAttributes.addFlashAttribute("success", "Room updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "Room deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
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
            Model model) {
        
        List<Room> rooms = roomService.getAllRooms();
        
        if (type != null) {
            rooms = roomService.getRoomsByType(type);
        }
        if (minPrice != null && maxPrice != null) {
            rooms = roomService.getRoomsByPriceRange(minPrice, maxPrice);
        }
        if (floor != null) {
            rooms = roomService.getRoomsByFloor(floor);
        }
        if (view != null) {
            rooms = roomService.getRoomsByView(view);
        }
        if (petFriendly != null && petFriendly) {
            rooms = roomService.getPetFriendlyRooms();
        }
        if (smoking != null && smoking) {
            rooms = roomService.getSmokingRooms();
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
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/cleaning")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String updateCleaningStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            roomService.updateRoomCleaningStatus(id, LocalDateTime.parse(status));
            redirectAttributes.addFlashAttribute("success", "Cleaning status updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }
} 