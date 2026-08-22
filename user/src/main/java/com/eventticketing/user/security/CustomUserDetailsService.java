package com.eventticketing.user.security;

import com.eventticketing.user.entity.User;
import com.eventticketing.user.exception.UserNotFoundException;
import com.eventticketing.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        Optional<User> optionalUser=userRepository.findByEmail(username);
        User user=optionalUser.orElseThrow(()->new UserNotFoundException("User Not found"));
        return new CustomUserDetails(user);
    }
}
