package com.example.webchat.service;

import com.example.webchat.enums.RoleName;
import com.example.webchat.enums.RoomType;
import com.example.webchat.model.User;
import com.example.webchat.repository.RoomRepository;
import com.example.webchat.model.Room;
import com.example.webchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

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
            // Set room name to the recipient's name for PERSONAL rooms
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

                        // Update both userIds and moderatorIds
                        room.setUserIds(updatedRoom.getUserIds());
                        room.setModeratorIds(updatedRoom.getModeratorIds());

                        // Ensure that all moderators are also in the userIds list
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

    public Optional<Room> getRoomById(String roomId) {
        return roomRepository.findById(roomId);
    }
}

