package com.example.webchat.model;

import com.example.webchat.enums.RoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "rooms")
public class Room {
    @Id
    private String id;
    private String name;
    private String description;
    private boolean isPrivate;
    private List<String> userIds;
    private String creatorId;
    private List<String> moderatorIds;
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    public Room() {}

    public Room(String name, String description, boolean isPrivate, List<String> userIds) {
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
        this.userIds = userIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
    public List<String> getModeratorIds() {
        return moderatorIds;
    }
    public void setModeratorIds(List<String> moderatorIds) {
        this.moderatorIds = moderatorIds;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
}


