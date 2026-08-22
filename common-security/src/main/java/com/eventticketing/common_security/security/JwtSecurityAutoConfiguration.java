package com.eventticketing.common_security.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;


@AutoConfiguration
public class JwtSecurityAutoConfiguration {

    @Bean
    public JwtTokenService jwtTokenService(
            @Value("${jwt.secret}") String secret) {

        return new JwtTokenService(secret);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenService jwtTokenService) {

        return new JwtAuthenticationFilter(jwtTokenService);
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }
}