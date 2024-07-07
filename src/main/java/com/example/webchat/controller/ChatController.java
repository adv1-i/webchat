package com.example.webchat.controller;

import com.example.webchat.model.Message;
import com.example.webchat.repository.MessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.Date;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, MessageRepository messageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message chatMessage, Principal principal) {
        Message message = new Message();
        message.setContent(chatMessage.getContent());
        message.setSender(principal.getName());
        message.setRecipients(chatMessage.getRecipients());
        message.setRoomId(chatMessage.getRoomId());
        message.setTimestamp(new Date());

        messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), message);
    }
}





