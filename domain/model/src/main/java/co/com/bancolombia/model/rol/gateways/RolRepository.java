package co.com.bancolombia.model.rol.gateways;

import co.com.bancolombia.model.rol.Rol;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface RolRepository {

    Mono<Rol> findByName(String name);
    Mono<Rol> findById(Long id);
}
