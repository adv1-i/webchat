package com.example.webchat.controller;

import com.example.webchat.UserStatusMessage;
import com.example.webchat.dto.UserStatusDTO;
import com.example.webchat.enums.UserStatus;
import com.example.webchat.model.User;
import com.example.webchat.repository.UserRepository;
import com.example.webchat.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class UserStatusController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public UserStatusController(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @MessageMapping("/user.status")
    public void updateUserStatus(UserStatusMessage userStatusMessage) {
        User user = userService.findByUsername(userStatusMessage.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userStatusMessage.getUsername()));

        if (!user.getStatus().equals(userStatusMessage.getStatus())) {
            user.setStatus(userStatusMessage.getStatus());
            userService.save(user);
            messagingTemplate.convertAndSend("/topic/user.status", new UserStatusDTO(user.getUsername(), user.getStatus()));
        }
    }

    @MessageMapping("/user.connect")
    public void handleUserConnect(Principal principal) {
        updateStatus(principal.getName(), UserStatus.ONLINE);
    }

    @MessageMapping("/user.disconnect")
    public void handleUserDisconnect(Principal principal) {
        updateStatus(principal.getName(), UserStatus.OFFLINE);
    }

    private void updateStatus(String username, UserStatus status) {
        User user = userService.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        user.setStatus(status);
        userService.save(user);
        messagingTemplate.convertAndSend("/topic/user.status", new UserStatusDTO(username, status));
    }
}

