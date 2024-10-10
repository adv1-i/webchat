package com.example.webchat.controller;

import com.example.webchat.model.Room;
import com.example.webchat.model.User;
import com.example.webchat.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> getRooms(Principal principal) {
        return ResponseEntity.ok(roomService.getAccessibleRooms(principal.getName()));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        return roomService.getRoomById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{roomId}/users")
    public ResponseEntity<List<User>> getRoomUsers(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.getRoomUsers(roomId));
    }

    @PostMapping("/{roomId}/users")
    public ResponseEntity<?> addUsersToRoom(@PathVariable String roomId, @RequestBody List<String> userIds, Principal principal) {
        return ResponseEntity.ok(roomService.addUsersToRoom(roomId, userIds, principal.getName()));
    }

    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        return roomService.getCreateRoomForm();
    }

    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestParam String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) Boolean isPrivate,
                                           @RequestParam(required = false) List<String> userIds,
                                           @RequestParam(required = false) List<String> moderatorIds,
                                           Principal principal) {
        Room room = roomService.prepareRoomForCreation(name, description, isPrivate, userIds, moderatorIds);
        return ResponseEntity.ok(roomService.createRoom(room, principal.getName()));
    }

    @GetMapping("/edit/{roomId}")
    public ModelAndView showEditForm(@PathVariable String roomId) {
        return roomService.getEditRoomForm(roomId);
    }

    @PostMapping("/edit/{roomId}")
    public ResponseEntity<Room> editRoom(@PathVariable String roomId,
                                         @RequestParam String name,
                                         @RequestParam(required = false) String description,
                                         @RequestParam(required = false) Boolean isPrivate,
                                         @RequestParam(required = false) List<String> userIds,
                                         @RequestParam(required = false) List<String> moderatorIds,
                                         Principal principal) {
        Room roomUpdate = roomService.prepareRoomForUpdate(roomId, name, description, isPrivate, userIds, moderatorIds);
        return roomService.updateRoom(roomId, roomUpdate, principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(roomService.getAllUsers());
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId, Principal principal) {
        roomService.deleteRoom(roomId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/details")
    public ResponseEntity<Map<String, Object>> getRoomDetails(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.getRoomDetails(roomId));
    }

    @GetMapping("/{roomId}/available-users")
    public ResponseEntity<List<User>> getAvailableUsers(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.getAvailableUsers(roomId));
    }

    @DeleteMapping("/{roomId}/users/{userId}")
    public ResponseEntity<?> removeUserFromRoom(@PathVariable String roomId, @PathVariable String userId, Principal principal) {
        roomService.removeUserFromRoom(roomId, userId, principal.getName());
        return ResponseEntity.ok().body(Map.of("message", "User removed successfully"));
    }
}