package com.example.webchat.service;

import com.example.webchat.enums.RoleName;
import com.example.webchat.enums.RoomType;
import com.example.webchat.model.User;
import com.example.webchat.repository.RoomRepository;
import com.example.webchat.model.Room;
import com.example.webchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Room> getAccessibleRooms(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (currentUser.getRole() == RoleName.ADMIN) {
            return roomRepository.findAll();
        } else {
            return roomRepository.findAll().stream()
                    .filter(room -> room.getUserIds().contains(currentUser.getId().toString()) ||
                            room.getCreatorId().equals(currentUser.getId().toString()) ||
                            room.getModeratorIds().contains(currentUser.getId().toString()))
                    .collect(Collectors.toList());
        }
    }

    public List<User> getRoomUsers(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        return room.getUserIds().stream()
                .map(userId -> userRepository.findById(Long.valueOf(userId)).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Room addUsersToRoom(String roomId, List<String> userIds, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (currentUser.getRole() == RoleName.ADMIN ||
                room.getCreatorId().equals(currentUser.getId().toString()) ||
                room.getModeratorIds().contains(currentUser.getId().toString())) {

            Set<String> updatedUserIds = new HashSet<>(room.getUserIds());
            updatedUserIds.addAll(userIds);
            room.setUserIds(new ArrayList<>(updatedUserIds));

            return roomRepository.save(room);
        } else {
            throw new AccessDeniedException("User does not have permission to add users to this room");
        }
    }

    public ModelAndView getCreateRoomForm() {
        ModelAndView modelAndView = new ModelAndView("room-form");
        modelAndView.addObject("room", new Room());
        modelAndView.addObject("users", userRepository.findAll());
        modelAndView.addObject("roles", RoleName.values());
        modelAndView.addObject("formAction", "/api/rooms/create");
        modelAndView.addObject("formMethod", "post");
        return modelAndView;
    }

    public Room prepareRoomForCreation(String name, String description, Boolean isPrivate, List<String> userIds, List<String> moderatorIds) {
        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        room.setPrivate(isPrivate != null ? isPrivate : false);
        room.setUserIds(userIds != null ? userIds : new ArrayList<>());
        room.setModeratorIds(moderatorIds != null ? moderatorIds : new ArrayList<>());
        return room;
    }

    public Room createRoom(Room room, String creatorUsername) {
        if (room.getId() == null || room.getId().isEmpty()) {
            room.setId(UUID.randomUUID().toString());
        }
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator not found: " + creatorUsername));
        room.setCreatorId(creator.getId().toString());
        room.setModeratorIds(Collections.singletonList(creator.getId().toString()));

        if (room.getUserIds().size() == 2 && !room.isPrivate()) {
            room.setRoomType(RoomType.PERSONAL);
            String recipientId = room.getUserIds().stream()
                    .filter(id -> !id.equals(creator.getId().toString()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user list for PERSONAL room"));
            User recipient = userRepository.findById(Long.valueOf(recipientId))
                    .orElseThrow(() -> new UsernameNotFoundException("Recipient not found"));
            room.setName(recipient.getUsername());
        } else if (room.isPrivate()) {
            room.setRoomType(RoomType.GROUP);
        } else {
            room.setRoomType(RoomType.PUBLIC);
        }

        return roomRepository.save(room);
    }

    public ModelAndView getEditRoomForm(String roomId) {
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

    public Room prepareRoomForUpdate(String roomId, String name, String description, Boolean isPrivate, List<String> userIds, List<String> moderatorIds) {
        Room roomUpdate = new Room();
        roomUpdate.setId(roomId);
        roomUpdate.setName(name);
        roomUpdate.setDescription(description);
        roomUpdate.setPrivate(isPrivate != null ? isPrivate : false);
        roomUpdate.setUserIds(userIds != null ? userIds : new ArrayList<>());
        roomUpdate.setModeratorIds(moderatorIds != null ? moderatorIds : new ArrayList<>());
        return roomUpdate;
    }

    public Optional<Room> updateRoom(String roomId, Room updatedRoom, String username) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                    if (user.getRole() == RoleName.ADMIN ||
                            room.getCreatorId().equals(user.getId().toString()) ||
                            room.getModeratorIds().contains(user.getId().toString())) {
                        room.setName(updatedRoom.getName());
                        room.setDescription(updatedRoom.getDescription());
                        room.setPrivate(updatedRoom.isPrivate());

                        room.setUserIds(updatedRoom.getUserIds());
                        room.setModeratorIds(updatedRoom.getModeratorIds());

                        Set<String> allUsers = new HashSet<>(room.getUserIds());
                        allUsers.addAll(room.getModeratorIds());

                        room.setUserIds(new ArrayList<>(allUsers));

                        if (room.getUserIds().size() == 2 && !room.isPrivate()) {
                            room.setRoomType(RoomType.PERSONAL);
                        } else if (room.isPrivate()) {
                            room.setRoomType(RoomType.GROUP);
                        } else {
                            room.setRoomType(RoomType.PUBLIC);
                        }

                        return roomRepository.save(room);
                    } else {
                        throw new AccessDeniedException("User does not have permission to update this room");
                    }
                });
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteRoom(String roomId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (currentUser.getRole() == RoleName.ADMIN ||
                room.getCreatorId().equals(currentUser.getId().toString())) {
            roomRepository.deleteById(roomId);
        } else {
            throw new AccessDeniedException("User does not have permission to delete this room");
        }
    }

    public Map<String, Object> getRoomDetails(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

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

        return details;
    }

    public List<User> getAvailableUsers(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> !room.getUserIds().contains(user.getId().toString()))
                .collect(Collectors.toList());
    }

    public void removeUserFromRoom(String roomId, String userId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

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
        } else {
            throw new AccessDeniedException("User does not have permission to remove users from this room");
        }
    }

    public Optional<Room> getRoomById(String roomId) {
        return roomRepository.findById(roomId);
    }
}