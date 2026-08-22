package com.eventticketing.user.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecurityAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityAccessDeniedHandler(ObjectMapper objectMapper){
        this.objectMapper=objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        Map<String,Object> errorResponse=new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now());

        errorResponse.put("status", HttpStatus.FORBIDDEN.value());

        errorResponse.put("error", "Forbidden");

        errorResponse.put("message", "You do not have permission to access this resource");

        response.getWriter().write(
                objectMapper.writeValueAsString(errorResponse)
        );


    }
}
