package com.eventticketing.user.config;

import com.eventticketing.common_security.security.JwtAuthenticationEntryPoint;
import com.eventticketing.common_security.security.JwtAuthenticationFilter;
import com.eventticketing.user.security.CustomUserDetailsService;
import com.eventticketing.user.security.SecurityAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityAccessDeniedHandler securityAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(SecurityAccessDeniedHandler securityAccessDeniedHandler,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint){
        this.securityAccessDeniedHandler=securityAccessDeniedHandler;
        this.jwtAuthenticationEntryPoint=jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/auth/register",
                        "/auth/login",
                        "/actuator/health",
                        "/actuator/health/**"
                ).permitAll()
                .anyRequest().authenticated())
                .exceptionHandling(exception->exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(securityAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService,
                                                         PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return  configuration.getAuthenticationManager();
    }
}
