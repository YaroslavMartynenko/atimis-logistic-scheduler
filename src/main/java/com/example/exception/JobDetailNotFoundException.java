package com.example.exception;

public class JobDetailNotFoundException extends RuntimeException{

    public JobDetailNotFoundException(String message) {
        super(message);
    }
}
