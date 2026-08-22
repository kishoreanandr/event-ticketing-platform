package com.eventticketing.user.controller;

import com.eventticketing.user.dto.LoginRequest;
import com.eventticketing.user.dto.LoginResponse;
import com.eventticketing.user.dto.UserRegistrationRequest;
import com.eventticketing.user.dto.UserResponse;
import com.eventticketing.user.exception.InvalidLoginException;
import com.eventticketing.user.exception.UserFoundException;
import com.eventticketing.user.service.AuthService;
import com.eventticketing.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final UserService userService;

    public AuthController(AuthService authService,UserService userService){
        this.authService=authService;
        this.userService=userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) throws InvalidLoginException {
        LoginResponse loginResponse=authService.login(loginRequest);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegistrationRequest registerRequest) throws UserFoundException {
        UserResponse registerResponse=userService.register(registerRequest);
        return new ResponseEntity<>(registerResponse,HttpStatus.CREATED);
    }
}
