package co.com.bancolombia.api.controller;

import co.com.bancolombia.api.Handler;
import co.com.bancolombia.api.dto.request.CreateUserDTO;
import co.com.bancolombia.api.dto.response.UserResponseDTO;
import co.com.bancolombia.api.mapper.UserDTOMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(
        title = "Documentación de Usuarios",
        version = "v1",
        description = "Endpoints para gestión de usuarios"))
public class UserController {

    private final Handler handler;
    private final UserDTOMapper mapper;

    @Operation(summary = "Crear usuario", tags = {"Usuarios"})
    @PreAuthorize("hasAnyRole('ADMIN','ASESOR')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponseDTO>> create(@Valid @RequestBody CreateUserDTO body) {
        return handler.createUser(body)
                .map(saved -> ResponseEntity
                        .created(URI.create("/api/v1/usuarios/" + saved.getUserId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.toDto(saved)));
    }

    @Operation(summary = "Listar Usuarios", tags = {"ListUsuarios"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<UserResponseDTO> listAllUser(){
        return handler.listAllUsers()
                .map(mapper::toDto);
    }

    @Operation(summary = "Obtener usuario por id", tags = {"Usuarios"})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponseDTO>> findUserById(@PathVariable("id")  Long id) {
        return handler.findUserById(id)
                .map(user -> ResponseEntity.ok(mapper.toDto(user)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Obtener usuario por email", tags = {"Usuarios"})
    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponseDTO>> findUserById(@PathVariable("email")  String email) {
        return handler.findUserByEmail(email)
                .map(user -> ResponseEntity.ok(mapper.toDto(user)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}