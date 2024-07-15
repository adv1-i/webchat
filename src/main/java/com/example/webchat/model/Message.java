package com.example.webchat.model;

import com.example.webchat.enums.MessageStatus;
import com.example.webchat.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private String content;
    private String sender;
    private String roomId;
    private Date timestamp;
    private List<String> recipients;
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



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public List<String> getFileIds() {
        return fileIds != null ? fileIds : new ArrayList<>();
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds != null ? fileIds : new ArrayList<>();
    }

    public List<String> getFileNames() {
        return fileNames != null ? fileNames : new ArrayList<>();
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames != null ? fileNames : new ArrayList<>();
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public void setIsForwarded(boolean forwarded) {
        isForwarded = forwarded;
    }

    public void setOriginalSender(String originalSender) {
        this.originalSender = originalSender;
    }

    public void setOriginalRoomId(String originalRoomId) {
        this.originalRoomId = originalRoomId;
    }

    public boolean isForwarded() {
        return isForwarded;
    }

    public String getOriginalSender() {
        return originalSender;
    }

    public String getOriginalRoomId() {
        return originalRoomId;
    }
}


