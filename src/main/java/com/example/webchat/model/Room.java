package com.example.webchat.model;

import com.example.webchat.enums.RoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}


