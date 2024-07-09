package com.example.webchat.service;

import com.example.webchat.enums.RoleName;
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

                        room.setUserIds(updatedRoom.getUserIds());
                        room.setModeratorIds(updatedRoom.getModeratorIds());

                        Set<String> allUsers = new HashSet<>(room.getUserIds());
                        allUsers.addAll(room.getModeratorIds());

                        room.setUserIds(new ArrayList<>(allUsers));

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

