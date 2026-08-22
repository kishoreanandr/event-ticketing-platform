package com.eventticketing.common_security.security;

public class JwtUserPrincipal {

    private final String username;
    private final Long userId;

    public JwtUserPrincipal(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return username;
    }
}