package co.com.bancolombia.api.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtPreAuthenticatedToken extends AbstractAuthenticationToken {
    private final String token;
    public JwtPreAuthenticatedToken(String token) {
        super(null);
        this.token = token;
        setAuthenticated(false);
    }
    @Override
    public Object getCredentials() { return token; }
    @Override
    public Object getPrincipal() { return null; }
}
