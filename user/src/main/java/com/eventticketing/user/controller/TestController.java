package com.eventticketing.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test(Authentication authentication) {

        return "Authenticated user: "
                + authentication.getName();
    }
    @GetMapping("/attendee-test")
    @PreAuthorize("hasRole('ATTENDEE')")
    public String attendeeTest() {
        return "Attendee access successful";
    }

    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest(){
        return "Admin Access successfully";
    }
}