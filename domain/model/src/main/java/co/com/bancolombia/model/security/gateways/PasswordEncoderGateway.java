package co.com.bancolombia.model.security.gateways;

import reactor.core.publisher.Mono;

public interface PasswordEncoderGateway {
    Mono<String> encode(String raw);
    Mono<Boolean> matches(String raw, String encoded);
}
