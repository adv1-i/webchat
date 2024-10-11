package com.example.webchat.service;

import com.example.webchat.enums.MessageType;
import com.example.webchat.enums.RoomType;
import com.example.webchat.model.Message;
import com.example.webchat.model.Room;
import com.example.webchat.repository.MessageRepository;
import com.example.webchat.repository.RoomRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(MessageRepository messageRepository, RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void processMessage(Message chatMessage, Principal principal) {
        Room room = roomRepository.findById(chatMessage.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Message message = createMessage(chatMessage, principal);

        setRecipients(message, room, principal);

        messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), message);
    }

    private Message createMessage(Message chatMessage, Principal principal) {
        Message message = new Message();
        message.setContent(chatMessage.getContent());
        message.setSender(principal.getName());
        message.setRoomId(chatMessage.getRoomId());
        message.setMessageType(MessageType.TEXT);
        message.setTimestamp(new Date());
        return message;
    }

    private void setRecipients(Message message, Room room, Principal principal) {
        List<String> recipients = new ArrayList<>(message.getRecipients());
        recipients.remove(principal.getName());

        if (room.getRoomType() == RoomType.PERSONAL) {
            String recipientId = room.getUserIds().stream()
                    .filter(id -> !id.equals(principal.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user list for PERSONAL room"));
            recipients = Collections.singletonList(recipientId);
        }

        message.setRecipients(recipients);
    }
}

