package com.example.webchat.service;

import com.example.webchat.enums.MessageStatus;
import com.example.webchat.exception.MessageNotFoundException;
import com.example.webchat.model.Message;
import com.example.webchat.repository.MessageRepository;
import com.example.webchat.utils.MessageStatusUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageStatusService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Message updateMessageStatus(String messageId, MessageStatus status, String updatedBy) throws MessageNotFoundException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found"));

        if (!message.getSender().equals(updatedBy)) {
            message.setMessageStatus(status);
            Message updatedMessage = messageRepository.save(message);

            messagingTemplate.convertAndSendToUser(
                    message.getSender(),
                    "/queue/message-status",
                    new MessageStatusUpdate(messageId, status)
            );

            return updatedMessage;
        }
        return message;
    }

    public void markMessagesAsDelivered(String roomId, String recipientId) throws MessageNotFoundException {
        List<Message> messages = messageRepository.findByRoomIdAndMessageStatus(roomId, MessageStatus.SENT);
        for (Message message : messages) {
            if (!message.getSender().equals(recipientId)) {
                updateMessageStatus(message.getId(), MessageStatus.DELIVERED, recipientId);
            }
        }
    }

    public void markMessagesAsRead(String roomId, String recipientId) throws MessageNotFoundException {
        List<Message> messages = messageRepository.findByRoomIdAndMessageStatusIn(roomId, List.of(MessageStatus.SENT, MessageStatus.DELIVERED));
        for (Message message : messages) {
            if (!message.getSender().equals(recipientId)) {
                updateMessageStatus(message.getId(), MessageStatus.READ, recipientId);
            }
        }
    }
}

