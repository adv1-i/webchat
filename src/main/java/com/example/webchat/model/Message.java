package com.example.webchat.model;

import com.example.webchat.enums.MessageStatus;
import com.example.webchat.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private String content;
    private String sender;
    private String roomId;
    private Date timestamp;
    private List<String> recipients = new ArrayList<>();
    private MessageType messageType;
    private MessageStatus messageStatus = MessageStatus.SENT;
    private List<String> fileIds = new ArrayList<>();
    private List<String> fileNames = new ArrayList<>();

    private List<EditHistory> editHistory = new ArrayList<>();
    @JsonProperty("isEdited")
    private boolean isEdited = false;

    @Transient
    private String formattedTime;

    @JsonProperty("isForwarded")
    private boolean isForwarded;
    @JsonProperty("originalSender")
    private String originalSender;
    @JsonProperty("originalRoomId")
    private String originalRoomId;

    public void addEditHistory(String oldContent, Date editTimestamp, List<String> fileIds, List<String> fileNames) {
        EditHistory editHistory = new EditHistory(oldContent, editTimestamp, fileIds, fileNames);
        this.editHistory.add(editHistory);
        this.isEdited = true;
    }
}


