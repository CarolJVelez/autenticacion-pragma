package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.CreateUserDTO;
import co.com.bancolombia.api.mapper.UserDTOMapper;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final UserUseCase userUseCase;
    private final UserDTOMapper mapper;
    private final LoggerRepository logger;

    public Mono<User> createUser(CreateUserDTO dto) {
        logger.info("POST /api/v1/usuarios recibido");
        return Mono.just(dto)
                .map(mapper::toModel)
                .flatMap(userUseCase::create)
                .doOnError(e -> logger.error("Error en createUser: {}", e.getMessage()));
    }
}