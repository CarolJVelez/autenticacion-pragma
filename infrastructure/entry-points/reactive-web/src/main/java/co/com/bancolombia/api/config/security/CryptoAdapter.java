package co.com.bancolombia.api.config.security;

import co.com.bancolombia.model.security.gateways.PasswordEncoderGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class CryptoAdapter implements PasswordEncoderGateway {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Mono<String> encode(String raw) {
        return Mono.fromCallable(() -> encoder.encode(raw))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> matches(String raw, String encoded) {
        return Mono.fromCallable(() -> encoder.matches(raw, encoded))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
