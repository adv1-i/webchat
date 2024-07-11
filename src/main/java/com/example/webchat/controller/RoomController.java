package com.example.webchat.controller;

import com.example.webchat.enums.RoleName;
import com.example.webchat.model.Room;
import com.example.webchat.model.User;
import com.example.webchat.repository.RoomRepository;
import com.example.webchat.repository.UserRepository;
import com.example.webchat.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> getRooms(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));

        List<Room> accessibleRooms;
        if (currentUser.getRole() == RoleName.ADMIN) {
            accessibleRooms = roomRepository.findAll();
        } else {
            accessibleRooms = roomRepository.findAll().stream()
                    .filter(room -> room.getUserIds().contains(currentUser.getId().toString()) ||
                            room.getCreatorId().equals(currentUser.getId().toString()) ||
                            room.getModeratorIds().contains(currentUser.getId().toString()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(accessibleRooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        return roomRepository.findById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{roomId}/users")
    public ResponseEntity<List<User>> getRoomUsers(@PathVariable String roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    List<User> users = room.getUserIds().stream()
                            .map(userId -> userRepository.findById(Long.valueOf(userId)).orElse(null))
                            .filter(user -> user != null)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(users);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{roomId}/users")
    public ResponseEntity<?> addUsersToRoom(@PathVariable String roomId, @RequestBody List<String> userIds, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));

        return roomRepository.findById(roomId)
                .map(room -> {
                    if (currentUser.getRole() == RoleName.ADMIN ||
                            room.getCreatorId().equals(currentUser.getId().toString()) ||
                            room.getModeratorIds().contains(currentUser.getId().toString())) {

                        Set<String> updatedUserIds = new HashSet<>(room.getUserIds());
                        updatedUserIds.addAll(userIds);
                        room.setUserIds(new ArrayList<>(updatedUserIds));

                        Room updatedRoom = roomRepository.save(room);
                        return ResponseEntity.ok(updatedRoom);
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("room-form");
        modelAndView.addObject("room", new Room());
        modelAndView.addObject("users", userRepository.findAll());
        modelAndView.addObject("roles", RoleName.values());
        modelAndView.addObject("formAction", "/api/rooms/create");
        modelAndView.addObject("formMethod", "post");
        return modelAndView;
    }

    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestParam String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) Boolean isPrivate,
                                           @RequestParam(required = false) List<String> userIds,
                                           @RequestParam(required = false) List<String> moderatorIds,
                                           Principal principal) {
        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        room.setPrivate(isPrivate != null ? isPrivate : false);
        room.setUserIds(userIds != null ? userIds : new ArrayList<>());
        room.setModeratorIds(moderatorIds != null ? moderatorIds : new ArrayList<>());

        return ResponseEntity.ok(roomService.createRoom(room, principal.getName()));
    }

    @GetMapping("/edit/{roomId}")
    public ModelAndView showEditForm(@PathVariable String roomId) {
        ModelAndView modelAndView = new ModelAndView("room-form");
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        modelAndView.addObject("room", room);
        modelAndView.addObject("users", userRepository.findAll());
        modelAndView.addObject("roles", RoleName.values());
        modelAndView.addObject("formAction", "/api/rooms/edit/" + roomId);
        modelAndView.addObject("formMethod", "post");
        return modelAndView;
    }



    @PostMapping("/edit/{roomId}")
    public ResponseEntity<Room> editRoom(@PathVariable String roomId,
                                         @RequestParam String name,
                                         @RequestParam(required = false) String description,
                                         @RequestParam(required = false) Boolean isPrivate,
                                         @RequestParam(required = false) List<String> userIds,
                                         @RequestParam(required = false) List<String> moderatorIds,
                                         Principal principal) {
        Room roomUpdate = new Room();
        roomUpdate.setId(roomId);
        roomUpdate.setName(name);
        roomUpdate.setDescription(description);
        roomUpdate.setPrivate(isPrivate != null ? isPrivate : false);
        roomUpdate.setUserIds(userIds != null ? userIds : new ArrayList<>());
        roomUpdate.setModeratorIds(moderatorIds != null ? moderatorIds : new ArrayList<>());

        return roomService.updateRoom(roomId, roomUpdate, principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));

        return roomRepository.findById(roomId)
                .map(room -> {
                    if (currentUser.getRole() == RoleName.ADMIN ||
                            room.getCreatorId().equals(currentUser.getId().toString())) {
                        roomRepository.deleteById(roomId);
                        return ResponseEntity.noContent().<Void>build();
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{roomId}/details")
    public ResponseEntity<Map<String, Object>> getRoomDetails(@PathVariable String roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("name", room.getName());
                    details.put("description", room.getDescription());

                    List<User> users = room.getUserIds().stream()
                            .map(userId -> userRepository.findById(Long.valueOf(userId)).orElse(null))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    details.put("users", users);
                    details.put("userCount", users.size());
                    details.put("moderatorIds", room.getModeratorIds());

                    return ResponseEntity.ok(details);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{roomId}/available-users")
    public ResponseEntity<List<User>> getAvailableUsers(@PathVariable String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        List<User> allUsers = userRepository.findAll();
        List<User> availableUsers = allUsers.stream()
                .filter(user -> !room.getUserIds().contains(user.getId().toString()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(availableUsers);
    }

    @DeleteMapping("/{roomId}/users/{userId}")
    public ResponseEntity<?> removeUserFromRoom(@PathVariable String roomId, @PathVariable String userId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));

        return roomRepository.findById(roomId)
                .map(room -> {
                    if (currentUser.getRole() == RoleName.ADMIN ||
                            room.getCreatorId().equals(currentUser.getId().toString()) ||
                            room.getModeratorIds().contains(currentUser.getId().toString())) {

                        List<String> updatedUserIds = new ArrayList<>(room.getUserIds());
                        updatedUserIds.remove(userId);
                        room.setUserIds(updatedUserIds);

                        List<String> updatedModeratorIds = new ArrayList<>(room.getModeratorIds());
                        updatedModeratorIds.remove(userId);
                        room.setModeratorIds(updatedModeratorIds);

                        roomRepository.save(room);
                        return ResponseEntity.ok().body(Map.of("message", "User removed successfully"));
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found"));
                    }
}