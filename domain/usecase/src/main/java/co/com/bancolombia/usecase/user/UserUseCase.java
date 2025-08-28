package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exceptions.*;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.userValidation.UserValidation;
import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;
    private final LoggerRepository logger;

    public Mono<User> create(User user) {
        logger.info("Inicio creacion de usuario con el email= {}", user.getEmail());
        return Mono.fromRunnable(() -> UserValidation.validate(user))
                .then(Mono.defer(() -> userRepository.existsByEmail(user.getEmail())))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        logger.info("Correo duplicado detectado: {}", user.getEmail());
                        return Mono.error(new DuplicateEmailException(user.getEmail()));
                    }
                    return userRepository.save(user);
                }).doOnSuccess(u -> logger.info("Usuario creado userId={}, email={}", u.getUserId(), u.getEmail()));
    }
/*
    public Mono<User> update(Long id, User user) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado: " + id)))
                .flatMap(ex -> userRepository.update(
                        ex.toBuilder()
                                .name(user.getName())
                                .lastName(user.getLastName())
                                .birthDate(user.getBirthDate())
                                .address(user.getAddress())
                                .phone(user.getPhone())
                                .email(user.getEmail())
                                .baseSalary(user.getBaseSalary())
                                .document(user.getDocument())
                                .build()
                ));
    }*/

    public Mono<User> findById(Long id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado por Id: " + id)));
    }

    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado por correo: " + email)));
    }

/*
    public Mono<Void> delete(Long id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado para eliminar: " + id))).then(userRepository.deleteById(id));
    }*/
}