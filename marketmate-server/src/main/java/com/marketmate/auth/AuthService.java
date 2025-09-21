package com.marketmate.auth;

import com.marketmate.user.User;
import com.marketmate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public User signup(String email, String rawPassword, String displayName) {
        users.findByEmailIgnoreCase(email).ifPresent(u -> {
            throw new IllegalArgumentException("既に登録済みのメールです");
        });
        var user = User.builder()
                .id(UUID.randomUUID())
                .email(email.trim())
                .password(encoder.encode(rawPassword))
                .displayName(displayName.trim())
                .build();
        return users.save(user);
    }

    public User login(String email, String rawPassword) {
        var user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("メールまたはパスワードが不正です"));
        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("メールまたはパスワードが不正です");
        }
        return user;
    }
}
