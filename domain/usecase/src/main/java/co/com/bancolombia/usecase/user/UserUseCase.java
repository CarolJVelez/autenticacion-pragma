package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exceptions.*;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.user.interfaces.IUserUseCase;
import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private final UserRepository userRepository;

    public Mono<User> create(User user) {
        //log.info("Creando usuario con el email= {}", user.getEmail());
        return userRepository.existsByEmail(user.getEmail()).flatMap(exists -> {
            if (Boolean.TRUE.equals(exists)) return Mono.error(new DuplicateEmailException(user.getEmail()));
            return userRepository.save(user);
        });//.doOnSuccess(u -> log.info("Usuario creado userId={}, email={}", u.getUserId(), u.getEmail()));
    }

    public Mono<User> update(String id, User user) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado: " + id)))
                .flatMap(ex -> userRepository.update(
                        ex.toBuilder()
                                .name(user.getName())
                                .lastname(user.getLastname())
                                .birthDate(user.getBirthDate())
                                .address(user.getAddress())
                                .phone(user.getPhone())
                                .email(user.getEmail())
                                .baseSalary(user.getBaseSalary())
                                .documentId(user.getDocumentId())
                                .build()
                ));
    }

    public Mono<User> getById(String id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado: " + id)));
    }

    public Flux<User> getAll() {
        return userRepository.findAll();
    }

    public Mono<Void> delete(String id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado: " + id))).then(userRepository.deleteById(id));
    }
}