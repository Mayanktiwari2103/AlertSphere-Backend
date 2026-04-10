package com.alertsphere.backend.security.oauth2;

import com.alertsphere.backend.model.User;
import com.alertsphere.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        System.out.println("🔥 OAuth HIT");
        System.out.println("📦 ATTRIBUTES: " + attributes);

        // ✅ SAFE EMAIL EXTRACTION
        String email = (String) attributes.get("email");

        if (email == null) {
            throw new OAuth2AuthenticationException("❌ Email not found from Google");
        }

        System.out.println("📧 Email: " + email);

        Optional<User> existingUser = userRepository.findByEmail(email);

        System.out.println("🔍 Exists in DB? " + existingUser.isPresent());

        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            System.out.println("✅ Existing user found: " + user.getId());
        } else {
            System.out.println("🆕 Creating NEW user in DB...");

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName((String) attributes.get("name"));

            // Role assignment
            if (email.equals("mayanktiwari2103@gmail.com")) {
                newUser.setRole("ADMIN");
            } else {
                newUser.setRole("USER");
            }

            // Dummy password (important)
            newUser.setPassword(
                    passwordEncoder.encode("OAUTH2_USER_" + UUID.randomUUID())
            );

            try {
                user = userRepository.save(newUser);
                System.out.println("✅ USER SAVED SUCCESSFULLY! ID: " + user.getId());
            } catch (Exception e) {
                System.out.println(" ERROR SAVING USER");
                e.printStackTrace();
                throw e;
            }
        }

        return oAuth2User;
    }
}