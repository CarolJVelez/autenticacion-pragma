package co.com.bancolombia.r2dbc.reactiveRepositoryRol;

import co.com.bancolombia.r2dbc.entities.RolEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;


public interface MyReactiveRepositoryRol extends ReactiveCrudRepository<RolEntity, Long>, ReactiveQueryByExampleExecutor<RolEntity> {

    Mono<RolEntity> findById(Long id);

    //@Query("SELECT * FROM roles WHERE nombre = UPPER(:nombre)")
    Mono<RolEntity> findByName(String name);
}
