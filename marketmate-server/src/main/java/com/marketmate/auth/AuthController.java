package com.marketmate.auth;

import com.marketmate.user.User;
import com.marketmate.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

import static com.marketmate.auth.CookieUtils.createHttpOnlyCookie;
import static com.marketmate.auth.CookieUtils.deleteCookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;
    private final JwtService jwt;
    private final UserRepository users;

    @Value("${marketmate.jwt.cookie-name}")
    String cookieName;
    @Value("${marketmate.jwt.expires-minutes}")
    long expMinutes;
    @Value("${app.cookie.secure:false}")
    boolean cookieSecure;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> signup(@RequestBody SignupReq req, HttpServletResponse res) {
        User u = auth.signup(req.email(), req.password(), req.displayName());
        // サインアップ後はそのままログインさせる
        var token = jwt.create(u.getId());
        res.addCookie(createHttpOnlyCookie(cookieName, token, (int) (expMinutes * 60), cookieSecure));
        return Map.of("id", u.getId(), "email", u.getEmail(), "name", u.getDisplayName());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginReq req, HttpServletResponse res) {
        User u = auth.login(req.email(), req.password());
        var token = jwt.create(u.getId());
        res.addCookie(createHttpOnlyCookie(cookieName, token, (int) (expMinutes * 60), cookieSecure));
        return Map.of("id", u.getId(), "email", u.getEmail(), "name", u.getDisplayName());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse res) {
        res.addCookie(deleteCookie(cookieName, cookieSecure));
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal UUID userId) {
        if (userId == null) return Map.of("authenticated", false);
        var u = users.findById(userId).orElseThrow();
        return Map.of("authenticated", true, "id", u.getId(), "email", u.getEmail(), "name", u.getDisplayName());
    }

    public record SignupReq(@Email String email,
                            @NotBlank String password,
                            @NotBlank String displayName) {
    }

    public record LoginReq(@Email String email,
                           @NotBlank String password) {
    }
}
