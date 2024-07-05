package com.example.webchat.exception;

public class RoomNotFoundException extends Exception {

    public RoomNotFoundException(String message) {
        System.out.println("Комната не найдена");
    }
}
