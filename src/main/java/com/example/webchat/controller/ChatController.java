package com.example.webchat.controller;

import com.example.webchat.model.Message;
import com.example.webchat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message chatMessage, Principal principal) {
        chatService.processMessage(chatMessage, principal);
    }
}






