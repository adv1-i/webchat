package com.example.webchat.controller;

import com.example.webchat.enums.MessageType;
import com.example.webchat.enums.RoomType;
import com.example.webchat.model.Message;
import com.example.webchat.model.Room;
import com.example.webchat.repository.MessageRepository;
import com.example.webchat.repository.RoomRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, MessageRepository messageRepository, RoomRepository roomRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message chatMessage, Principal principal) {
        Room room = roomRepository.findById(chatMessage.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Message message = new Message();
        message.setContent(chatMessage.getContent());
        message.setSender(principal.getName());
        message.setRoomId(chatMessage.getRoomId());
        message.setMessageType(MessageType.TEXT);
        message.setTimestamp(new Date());

        List<String> recipients = new ArrayList<>(chatMessage.getRecipients());
        recipients.remove(principal.getName());
        message.setRecipients(recipients);

        if (room.getRoomType() == RoomType.PERSONAL) {
            String recipientId = room.getUserIds().stream()
                    .filter(id -> !id.equals(principal.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user list for PERSONAL room"));
            message.setRecipients(Collections.singletonList(recipientId));
        }

        messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), message);
    }
}





