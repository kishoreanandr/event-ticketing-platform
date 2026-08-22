package com.eventticketing.user.service;

import com.eventticketing.user.dto.LoginRequest;
import com.eventticketing.user.dto.LoginResponse;
import com.eventticketing.user.entity.User;
import com.eventticketing.user.exception.UserNotFoundException;
import com.eventticketing.user.repository.UserRepository;
import com.eventticketing.user.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService){
        this.authenticationManager=authenticationManager;
        this.jwtService=jwtService;
    }

    public LoginResponse login(LoginRequest loginRequest){
        UsernamePasswordAuthenticationToken authenticationToken=
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword());
        Authentication authentication= authenticationManager.authenticate(authenticationToken);
        CustomUserDetails userDetails= (CustomUserDetails) authentication.getPrincipal();
        String accessToken=jwtService.generateToken(userDetails);

        return new LoginResponse(accessToken,"Bearer");

    }
}
