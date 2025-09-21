package com.marketmate.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.UUID;

public class JwtAuth extends AbstractAuthenticationToken {
    private final UUID userId;

    public JwtAuth(UUID userId) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.userId = userId;
        setAuthenticated(true);
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}
