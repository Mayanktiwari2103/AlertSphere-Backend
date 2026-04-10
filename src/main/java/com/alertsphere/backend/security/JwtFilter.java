package com.alertsphere.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JwtUtil jwt;



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String header = request.getHeader("Authorization");

        // 1. If there is a Bearer token, try to validate it NO MATTER the path
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwt.validate(token)) {
                    String email = jwt.extractEmail(token);
                    // Set the security context so the Controller can see the Principal
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Log error but don't block yet; SecurityConfig will handle permissions
                System.out.println("JWT Validation failed: " + e.getMessage());
            }
        }

        // 2. FORWARD the request to the next filter/controller
        // SecurityConfig.java will now decide if the path is permitted or not
        chain.doFilter(request, response);
    }}