package co.com.bancolombia.model.security.gateways;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> generateToken(User user);
    Mono<Boolean> validateToken(String token);
    //Mono<Long> getUserId(String token);
    Mono<String> getRole(String token);
    Mono<String> getSubject(String token);
    Mono<Long> getExpirationSeconds();
}
