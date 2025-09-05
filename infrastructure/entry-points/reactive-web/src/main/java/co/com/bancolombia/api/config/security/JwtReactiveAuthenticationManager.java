package co.com.bancolombia.api.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final SecretKey key;

    public JwtReactiveAuthenticationManager(JwtProperties props) {
        byte[] bytes = props.getSecret().matches("^[A-Za-z0-9+/=]+$")
                ? Decoders.BASE64.decode(props.getSecret())
                : props.getSecret().getBytes();
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                        .parseClaimsJws(token).getBody();

                String role = String.valueOf(claims.get("role"));
                String sub  = claims.getSubject();

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                Authentication auth =
                        new UsernamePasswordAuthenticationToken(sub, token, authorities);

                if (auth instanceof org.springframework.security.authentication.AbstractAuthenticationToken aat) {
                    aat.setDetails(Map.of("role", role, "sub", sub));
                }

                return auth;
            } catch (Exception e) {
                throw new BadCredentialsException("Token inválido");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}