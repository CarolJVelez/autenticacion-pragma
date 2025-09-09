package co.com.bancolombia.api.config.security;

import co.com.bancolombia.model.security.gateways.TokenProvider;
import co.com.bancolombia.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        byte[] bytes = props.getSecret().matches("^[A-Za-z0-9+/=]+$")
                ? Decoders.BASE64.decode(props.getSecret())
                : props.getSecret().getBytes();
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public Mono<String> generateToken(User user) {
        return Mono.fromCallable(() -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole() != null ? user.getRole().getName() : "CLIENTE");
            claims.put("name", user.getName());
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(props.getExpiration());
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuer(props.getIssuer())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(exp))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
/*
    @Override
    public Mono<Long> getUserId(String token) {
        return Mono.fromCallable(() -> parseClaims(token).get("uid", Number.class).longValue())
                .subscribeOn(Schedulers.boundedElastic());
    }*/

    @Override
    public Mono<String> getRole(String token) {
        return Mono.fromCallable(() -> parseClaims(token).get("role", String.class))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> getSubject(String token) {
        return Mono.fromCallable(() -> parseClaims(token).getSubject())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> getExpirationSeconds() {
        return Mono.just(props.getExpiration());
    }
}
