package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserRepository {
    Mono<User> save(User u);

    Mono<User> update(User u);

    Mono<Boolean> existsByEmail(String e);

    Mono<User> findById(Long id);

    Mono<User> findByEmail(String email);

    Flux<User> findAll();

    Mono<Void> deleteById(Long id);

    Flux<User> findAllById(Collection<Long> ids);

}
