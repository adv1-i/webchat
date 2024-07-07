package com.example.webchat;

import com.example.webchat.enums.UserStatus;

public class UserStatusMessage {
    private String username;
    private UserStatus status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }
}

