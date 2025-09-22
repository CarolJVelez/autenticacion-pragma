package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exceptions.*;
import co.com.bancolombia.model.rol.Rol;
import co.com.bancolombia.model.rol.gateways.RolRepository;
import co.com.bancolombia.model.security.gateways.PasswordEncoderGateway;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.userValidation.UserValidation;
import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;
    private final LoggerRepository logger;
    private final PasswordEncoderGateway passwordEncoder;
    private final UserValidation userValidation;
    private final RolRepository rolRepository;

    public Mono<User> create(User user) {
        logger.info("Inicio creacion de usuario con el email= {}", user.getEmail());

        return userValidation.validate(user)
                .then(userValidation.validateUserExists(user.getEmail()))
                .then(userValidation.validateUserExistsForDocument(user.getDocument()))
                .then(rolRepository.findByName(user.getRole().getName()))
                .flatMap(rol -> passwordEncoder.encode(user.getPassword())
                        .map(hash -> user.toBuilder()
                                .password(hash)
                                .role(rol)
                                .maxIndebtedness(userValidation.calcularCapacidadEndeudamiento(user.getBaseSalary()))
                                .build()
                        )
                )
                .flatMap(userRepository::save)
                .doOnSuccess(u -> logger.info("Usuario creado exitosamente userId={}, email={}",
                        u.getUserId(), u.getEmail()))
                .doOnError(error -> logger.error("Error al crear usuario con email={}: {}",
                        user.getEmail(), error.getMessage(), error));
    }




    public Mono<User> findById(Long id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado por Id: " + id)));
    }

    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado por correo: " + email)));
    }

    public Flux<User> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            logger.warn("findByIds llamado con lista vacía o nula");
            return Flux.empty();
        }

        List<Long> distinctOrdered = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return userRepository.findAllById(distinctOrdered)
                .collectMap(User::getUserId)
                .flatMapMany(foundMap -> {
                    var foundIds = foundMap.keySet();
                    var missing = distinctOrdered.stream()
                            .filter(id -> !foundIds.contains(id))
                            .toList();
                    if (!missing.isEmpty()) {
                        logger.warn("Usuarios no encontrados para IDs: {}", missing);
                    } else {
                        logger.info("findByIds: todos los usuarios encontrados. Total={}", foundIds.size());
                    }

                    var orderedFound = ids.stream()
                            .map(foundMap::get)
                            .filter(Objects::nonNull)
                            .toList();

                    return orderedFound.isEmpty()
                            ? Flux.empty()
                            : Flux.fromIterable(orderedFound);
                });
    }


}