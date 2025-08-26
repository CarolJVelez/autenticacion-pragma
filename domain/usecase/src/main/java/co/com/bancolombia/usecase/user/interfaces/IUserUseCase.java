package co.com.bancolombia.usecase.user.interfaces;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserUseCase {
    Mono<User> create(User u);

    Mono<User> update(String id, User u);

    Mono<User> getById(String id);

    Flux<User> getAll();

    Mono<Void> delete(String id);
}