package com.eventticketing.common_security.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Long getCurrentUserId(){
        Authentication authentication=
                SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null || !authentication.isAuthenticated()){
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal=authentication.getPrincipal();

        if(!(principal instanceof JwtUserPrincipal jwtUserPrincipal)){
            throw new IllegalStateException(
                    "Authenticated principal does not contain JWT user information"
            );
        }
        Long userId = jwtUserPrincipal.getUserId();

        if(userId==null){
            throw new IllegalStateException(
                    "JWT does not contain userId"
            );
        }
        return userId;
    }

    public static String getCurrentUserName(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof JwtUserPrincipal jwtUserPrincipal)) {

            throw new IllegalStateException(
                    "Authenticated principal does not contain JWT user information"
            );
        }

        return jwtUserPrincipal.getUsername();

    }

}
