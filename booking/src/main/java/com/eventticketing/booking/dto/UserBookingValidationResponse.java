package com.eventticketing.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class UserBookingValidationResponse {
    private Long userId;
    private boolean exists;
    private boolean active;
    private Set<RoleName> roles;

}
