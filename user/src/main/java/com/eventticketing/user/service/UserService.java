package com.eventticketing.user.service;

import com.eventticketing.user.dto.UserBookingValidationResponse;
import com.eventticketing.user.dto.UserRegistrationRequest;
import com.eventticketing.user.dto.UserResponse;
import org.springframework.stereotype.Service;

public interface UserService {
    UserResponse register(UserRegistrationRequest userRegistrationRequest);
    UserBookingValidationResponse validateUserForBooking(Long userId);

}
