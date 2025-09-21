package com.marketmate.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final Algorithm alg;
    private final long expiresMinutes;

    public JwtService(
            @Value("${marketmate.jwt.secret}") String secret,
            @Value("${marketmate.jwt.expires-minutes}") long expiresMinutes
    ) {
        this.alg = Algorithm.HMAC256(secret);
        this.expiresMinutes = expiresMinutes;
    }

    public String create(UUID userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(expiresMinutes, ChronoUnit.MINUTES);
        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withIssuer("marketmate")
                .sign(alg);
    }

    public UUID verifyAndGetUserId(String token) {
        var verifier = JWT.require(alg).withIssuer("marketmate").build();
        var decoded = verifier.verify(token);
        return UUID.fromString(decoded.getSubject());
    }
}
