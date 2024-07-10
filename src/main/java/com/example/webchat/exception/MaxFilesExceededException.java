package com.example.webchat.exception;

public class MaxFilesExceededException extends Exception {

    public MaxFilesExceededException(String message) {
        super(message);
    }
}
