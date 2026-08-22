package com.eventticketing.user.service;

import com.eventticketing.user.dto.UserBookingValidationResponse;
import com.eventticketing.user.dto.UserRegistrationRequest;
import com.eventticketing.user.dto.UserResponse;
import com.eventticketing.user.entity.Role;
import com.eventticketing.user.entity.RoleName;
import com.eventticketing.user.entity.User;
import com.eventticketing.user.entity.UserStatus;
import com.eventticketing.user.exception.RoleNotFoundException;
import com.eventticketing.user.exception.UserFoundException;
import com.eventticketing.user.repository.RoleRepository;
import com.eventticketing.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,RoleRepository roleRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.roleRepository=roleRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @Override
    public UserResponse register(UserRegistrationRequest userRegistrationRequest) {
        Optional<User> user=userRepository.findByEmail(userRegistrationRequest.getEmail());

        if(user.isPresent()){
            throw new UserFoundException("User already Exists!...");
        }
        
        User newUser=new User();
        newUser.setEmail(userRegistrationRequest.getEmail());
        newUser.setPhone(userRegistrationRequest.getPhone());
        newUser.setFirstName(userRegistrationRequest.getFirstName());
        newUser.setLastName(userRegistrationRequest.getLastName());
        newUser.setPasswordHash(passwordEncoder.encode(userRegistrationRequest.getPassword()));
        newUser.setStatus(UserStatus.ACTIVE);
        Role attendeeRole=roleRepository
                .findByRoleName(RoleName.ATTENDEE)
                .orElseThrow(()->new RoleNotFoundException("Role not found!..."));
        newUser.getRoles().add(attendeeRole);
        
        User userRegister=userRepository.save(newUser);

        return getUserResponse(userRegister);
    }

    @Override
    public UserBookingValidationResponse validateUserForBooking(Long userId) {
        return userRepository.findById(userId)
                .map(user-> new UserBookingValidationResponse(
                        user.getId(),
                        true,
                        user.getStatus()==UserStatus.ACTIVE,
                        user.getRoles()
                                .stream()
                                .map(Role::getRoleName)
                                .collect(Collectors.toSet())
                ))
                .orElseGet(()->new UserBookingValidationResponse(
                        userId,
                        false,
                        false,
                        null
                ));
    }

    private static UserResponse getUserResponse(User userRegister) {
        UserResponse userResponse=new UserResponse();
        userResponse.setId(userRegister.getId());
        userResponse.setPhone(userRegister.getPhone());
        userResponse.setEmail(userRegister.getEmail());
        userResponse.setUserStatus(userRegister.getStatus());
        userResponse.setRoles(userRegister.getRoles());
        userResponse.setCreatedAt(userRegister.getCreatedAt());
        userResponse.setFirstName(userRegister.getFirstName());
        userResponse.setLastName(userRegister.getLastName());
        return userResponse;
    }
}
