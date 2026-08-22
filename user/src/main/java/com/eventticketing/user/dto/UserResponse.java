package com.eventticketing.user.dto;

import com.eventticketing.user.entity.Role;
import com.eventticketing.user.entity.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserStatus userStatus;
    private Set<Role> roles;
    private LocalDateTime createdAt;
}
