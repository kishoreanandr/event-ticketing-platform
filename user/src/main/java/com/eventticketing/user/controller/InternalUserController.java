package com.eventticketing.user.controller;

import com.eventticketing.common_security.security.SecurityUtils;
import com.eventticketing.user.dto.UserBookingValidationResponse;
import com.eventticketing.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService){
        this.userService=userService;
    }

    @GetMapping("/{userId}/booking-validation")
    public ResponseEntity<UserBookingValidationResponse> validateUserForBooking(
            @PathVariable Long userId){

        Long authenticatedUserId= SecurityUtils.getCurrentUserId();

        if(!authenticatedUserId.equals(userId)){
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        UserBookingValidationResponse response=userService.validateUserForBooking(userId);
        return ResponseEntity.ok(response);
    }
}
