package com.example.webchat.utils;

import com.example.webchat.enums.MessageStatus;

public class MessageStatusUpdate {
    private String messageId;
    private MessageStatus status;

    public MessageStatusUpdate(String messageId, MessageStatus status) {
        this.messageId = messageId;
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
