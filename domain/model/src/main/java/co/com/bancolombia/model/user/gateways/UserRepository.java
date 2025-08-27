package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User u);

    Mono<User> update(User u);

    Mono<Boolean> existsByEmail(String e);

    Mono<User> findById(Long id);

    Flux<User> findAll();

    Mono<Void> deleteById(Long id);

}
