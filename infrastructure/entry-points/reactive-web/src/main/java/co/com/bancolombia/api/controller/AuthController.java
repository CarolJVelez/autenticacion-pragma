package co.com.bancolombia.api.controller;

import co.com.bancolombia.api.dto.request.LoginRequest;
import co.com.bancolombia.api.dto.response.LoginResponse;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.usecase.auth.AuthUseCase;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(
        title = "Autenticacion de Usuarios",
        version = "v1",
        description = "Endpoints para login de usuarios"))
public class AuthController {

    private final AuthUseCase authUseCase;
    private final LoggerRepository logger;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        logger.info("POST /api/v1/login recibido para email={}", request.getEmail());
        return authUseCase.login(request.getEmail(), request.getPassword())
                .map(ar -> ResponseEntity.ok(LoginResponse.builder()
                        .token(ar.getToken())
                        .tokenType(ar.getTokenType())
                        .expiresIn(ar.getExpiresIn())
                        .build()));
    }
}
