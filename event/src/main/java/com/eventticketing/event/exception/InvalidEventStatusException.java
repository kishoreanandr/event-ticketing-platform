package com.eventticketing.event.exception;

public class InvalidEventStatusException extends RuntimeException{
    public InvalidEventStatusException(String message){
        super(message);
    }
}
