package co.com.bancolombia.api.controller;

import co.com.bancolombia.model.exceptions.DuplicateEmailException;
import co.com.bancolombia.model.exceptions.NotFoundException;
import co.com.bancolombia.model.rol.Rol;
import co.com.bancolombia.model.rol.gateways.RolRepository;
import co.com.bancolombia.model.security.gateways.PasswordEncoderGateway;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.user.UserUseCase;
import co.com.bancolombia.usecase.userValidation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private LoggerRepository logger;
    @Mock private PasswordEncoderGateway passwordEncoder;
    @Mock private RolRepository rolRepository;

    private UserValidation userValidation;

    private UserUseCase userUseCase;

    private User baseUser;
    private Rol baseRol;

    @BeforeEach
    void init() {
        userValidation = new UserValidation(userRepository, logger);
        userUseCase = new UserUseCase(userRepository, logger, passwordEncoder, userValidation, rolRepository);

        baseRol = Rol.builder()
                .roleId(10L)
                .name("ASESOR")
                .description("Asesor comercial")
                .build();

        baseUser = User.builder()
                .userId(1L)
                .name("Carol")
                .lastName("Velez")
                .email("carol@example.com")
                .password("secret")
                .baseSalary(BigInteger.valueOf(4_000_000))
                .role(baseRol)
                .build();
    }

    @Test
    void create_duplicateEmail_shouldErrorDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));
        when(rolRepository.findByName(anyString())).thenReturn(Mono.never());
        StepVerifier.create(userUseCase.create(baseUser))
                .expectError(DuplicateEmailException.class)
                .verify();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_ok_shouldSaveAndReturn() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(rolRepository.findByName(eq("ASESOR"))).thenReturn(Mono.just(baseRol));
        when(passwordEncoder.encode(eq("secret"))).thenReturn(Mono.just("hashed-pass"));
        when(userRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userUseCase.create(baseUser))
                .assertNext(u -> {
                    assertEquals(baseUser.getEmail(), u.getEmail());
                    assertEquals("hashed-pass", u.getPassword());
                    assertNotNull(u.getRole());
                    assertEquals("ASESOR", u.getRole().getName());
                })
                .verifyComplete();

        verify(rolRepository).findByName("ASESOR");
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(any());
        verify(logger, atLeastOnce()).info(anyString(), any());
    }

    @Test
    void findAll_shouldReturnFlux() {
        User u2 = baseUser.toBuilder().userId(2L).email("c@example.com").build();
        when(userRepository.findAll()).thenReturn(Flux.just(baseUser, u2));

        StepVerifier.create(userUseCase.findAll())
                .expectNext(baseUser)
                .expectNext(u2)
                .verifyComplete();
    }

    @Test
    void findByEmail_missing_shouldErrorNotFound() {
        when(userRepository.findByEmail(eq("missing@example.com"))).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.findByEmail("missing@example.com"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void findById_ok_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(baseUser));

        StepVerifier.create(userUseCase.findById(1L))
                .expectNext(baseUser)
                .verifyComplete();
    }

    @Test
    void findById_missing_shouldErrorNotFound() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.findById(99L))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void findByEmail_ok_shouldReturnUser() {
        when(userRepository.findByEmail(eq(baseUser.getEmail()))).thenReturn(Mono.just(baseUser));

        StepVerifier.create(userUseCase.findByEmail(baseUser.getEmail()))
                .expectNext(baseUser)
                .verifyComplete();
    }

    @Test
    void create_roleNotFound_shouldError() {
        // No duplicado
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        // Rol no encontrado -> error
        when(rolRepository.findByName(eq("ASESOR"))).thenReturn(Mono.error(new NotFoundException("role not found")));

        StepVerifier.create(userUseCase.create(baseUser))
                .expectError(NotFoundException.class)
                .verify();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_encoderFails_shouldError() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(rolRepository.findByName(eq("ASESOR"))).thenReturn(Mono.just(baseRol));
        when(passwordEncoder.encode(eq("secret"))).thenReturn(Mono.error(new RuntimeException("enc-fail")));

        StepVerifier.create(userUseCase.create(baseUser))
                .expectErrorSatisfies(ex ->
                        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("enc-fail")))
                .verify();

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_saveFails_shouldError() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(rolRepository.findByName(eq("ASESOR"))).thenReturn(Mono.just(baseRol));
        when(passwordEncoder.encode(eq("secret"))).thenReturn(Mono.just("hashed-pass"));
        when(userRepository.save(any())).thenReturn(Mono.error(new RuntimeException("db-down")));

        StepVerifier.create(userUseCase.create(baseUser))
                .expectErrorSatisfies(ex ->
                        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("db-down")))
                .verify();
    }

}