package com.example.exception;

public class TriggerNotFoundException extends RuntimeException {

    public TriggerNotFoundException(String message) {
        super(message);
    }
}
