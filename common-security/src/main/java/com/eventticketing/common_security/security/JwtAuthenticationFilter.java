package com.eventticketing.common_security.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }
        String token = authorizationHeader.substring(7);

        if (!jwtTokenService.isTokenValid(token)) {
            SecurityContextHolder.clearContext();

            JwtAuthenticationEntryPoint entryPoint =
                    new JwtAuthenticationEntryPoint();

            entryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid or expired JWT token")
            );
            return;
        }

        String username =
                jwtTokenService.extractUsername(token);
        Long userId=
                jwtTokenService.extractUserId(token);

        List<SimpleGrantedAuthority> authorities =
                jwtTokenService.extractRoles(token)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
        JwtUserPrincipal principal=new JwtUserPrincipal(username,userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
