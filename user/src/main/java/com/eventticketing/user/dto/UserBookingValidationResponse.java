package com.eventticketing.user.dto;

import com.eventticketing.user.entity.RoleName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class UserBookingValidationResponse {
    private Long userId;
    private boolean exists;
    private boolean active;
    private Set<RoleName> roles;

}
