package com.alertsphere.backend.security.oauth2;

import com.alertsphere.backend.model.User;
import com.alertsphere.backend.repository.UserRepository;
import com.alertsphere.backend.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; // Add this
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // To set a dummy password for the DB

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 🚨 1. FIND OR CREATE USER IN DATABASE
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);

            // 🚨 THE FIX: Set a placeholder to satisfy the Validation
            newUser.setPhoneNumber("+910000000000");

            // Assign ADMIN role if it's your specific email
            if ("mayanktiwari2103@gmail.com".equals(email)) {
                newUser.setRole("ADMIN");
            } else {
                newUser.setRole("USER");
            }

            // Set a random password because the User model likely requires it
            newUser.setPassword(passwordEncoder.encode("GOOGLE_USER_" + UUID.randomUUID()));

            System.out.println("NEW USER REGISTERED VIA GOOGLE: " + email);
            return userRepository.save(newUser);
        });

        // 🚨 2. USE THE ACTUAL ROLE FROM THE SAVED USER OBJECT
        String token = jwtUtil.generateToken(email, user.getRole());

        // 3. Build the redirect URL
        String targetUrl = UriComponentsBuilder.fromUriString("https://alert-sphere-tau.vercel.app/")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}