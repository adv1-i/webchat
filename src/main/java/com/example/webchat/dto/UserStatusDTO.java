package com.example.webchat.dto;

import com.example.webchat.enums.UserStatus;

public class UserStatusDTO {
    private String username;
    private UserStatus status;

    public UserStatusDTO(String username, UserStatus status) {
        this.username = username;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
