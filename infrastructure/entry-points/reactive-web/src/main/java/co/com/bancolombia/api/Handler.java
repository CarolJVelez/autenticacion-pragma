package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.CreateUserDTO;
import co.com.bancolombia.api.mapper.UserDTOMapper;
import co.com.bancolombia.usecase.user.interfaces.IUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@RequiredArgsConstructor
public class Handler {

    private final IUserUseCase userUseCase;
    private final RequestValidator validator;
    private final UserDTOMapper mapper;


    public Mono<ServerResponse> createUser(ServerRequest req) {
        return req.bodyToMono(CreateUserDTO.class)
                .flatMap(validator::validate)
                .map(mapper::toModel)
                .flatMap(userUseCase::create)
                .flatMap(saved -> ServerResponse
                        .created(URI.create("/api/users/" + saved.getUserId()))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(mapper.toDto(saved))
                );
    }
}
