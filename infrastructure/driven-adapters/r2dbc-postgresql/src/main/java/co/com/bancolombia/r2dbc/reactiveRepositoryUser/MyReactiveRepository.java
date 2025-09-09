package co.com.bancolombia.r2dbc.reactiveRepositoryUser;

import co.com.bancolombia.r2dbc.entities.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface MyReactiveRepository extends ReactiveCrudRepository<UserEntity, Long>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Void> deleteByUserId(Long userId);

    Mono<Boolean> existsByEmail(String email);

    @Query("SELECT * FROM usuarios WHERE email = :email")
    Mono<UserEntity> findByEmail(String email);

    Flux<UserEntity> findByUserIdIn(Collection<Long> ids);

}
