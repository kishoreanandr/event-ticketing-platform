package com.eventticketing.user.exception;

import javax.security.auth.login.LoginException;

public class InvalidLoginException extends LoginException {
    public InvalidLoginException(String message){
        super(message);
    }
}
