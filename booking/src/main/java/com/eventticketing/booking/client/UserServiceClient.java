package com.eventticketing.booking.client;

import com.eventticketing.booking.dto.UserBookingValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service",url = "${services.user.url}")
public interface UserServiceClient {
    @GetMapping("internal/users/{userId}/booking-validation")
    UserBookingValidationResponse validateUserForBooking(
            @PathVariable("userId") Long userId);
}
