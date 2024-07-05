package com.example.webchat.config;

import com.example.webchat.dto.UserStatusDTO;
import com.example.webchat.enums.UserStatus;
import com.example.webchat.model.User;
import com.example.webchat.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @MessageMapping("/user.connect")
    public void handleUserConnect(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();
        user.setStatus(UserStatus.ONLINE);
        userService.save(user);

        broadcastUserStatus(user);
    }

    @MessageMapping("/user.disconnect")
    public void handleUserDisconnect(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();
        user.setStatus(UserStatus.OFFLINE);
        userService.save(user);

        broadcastUserStatus(user);
    }

    private void broadcastUserStatus(User user) {
        messagingTemplate.convertAndSend("/topic/user.status", new UserStatusDTO(user.getUsername(), user.getStatus()));
    }
}
