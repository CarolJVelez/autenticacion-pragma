package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserRepository {
    Mono<User> save(User u);

    Mono<Boolean> existsByEmail(String e);

    Mono<User> findById(Long id);

    Mono<User> findByEmail(String email);

    Mono<Boolean> findByDocument(String document);

    Flux<User> findAll();

    Flux<User> findAllById(Collection<Long> ids);

}
