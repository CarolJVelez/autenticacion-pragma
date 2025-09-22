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
import java.util.Arrays;
import java.util.List;

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

    private User u1, u2, u3;



    @BeforeEach
    void setUp() {
        baseRol = Rol.builder().roleId(1L).name("ASESOR").description("asesor").build();

        u1 = User.builder().userId(10L).email("a@a.com").password("p1").name("A").lastName("1").role(baseRol).build();
        u2 = User.builder().userId(20L).email("b@b.com").password("p2").name("B").lastName("2").role(baseRol).build();
        u3 = User.builder().userId(30L).email("c@c.com").password("p3").name("C").lastName("3").role(baseRol).build();
    }

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
        when(userRepository.findByDocument(any())).thenReturn(Mono.empty());
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
        when(userRepository.findByDocument(any())).thenReturn(Mono.empty());
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
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        // Evita NPE en validateUserExistsForDocument
        when(userRepository.findByDocument(any())).thenReturn(Mono.empty());
        // Forzamos el error del rol
        when(rolRepository.findByName(eq("ASESOR")))
                .thenReturn(Mono.error(new NotFoundException("role not found")));

        StepVerifier.create(userUseCase.create(baseUser))
                .expectError(NotFoundException.class)
                .verify();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_encoderFails_shouldError() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        // Evita NPE en validateUserExistsForDocument
        when(userRepository.findByDocument(any())).thenReturn(Mono.empty());
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
        when(userRepository.findByDocument(any())).thenReturn(Mono.empty());
        when(rolRepository.findByName(eq("ASESOR"))).thenReturn(Mono.just(baseRol));
        when(passwordEncoder.encode(eq("secret"))).thenReturn(Mono.just("hashed-pass"));
        when(userRepository.save(any())).thenReturn(Mono.error(new RuntimeException("db-down")));

        StepVerifier.create(userUseCase.create(baseUser))
                .expectErrorSatisfies(ex ->
                        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("db-down")))
                .verify();
    }



    @Test
    void getByIds_preservesOrder_andSkipsMissing() {
        List<Long> ids = Arrays.asList(30L, 99L, 10L);
        when(userRepository.findAllById(eq(ids))).thenReturn(Flux.just(u1, u3));

        StepVerifier.create(userUseCase.findByIds(ids))
                .expectNext(u3)
                .expectNext(u1)
                .verifyComplete();
    }

    @Test
    void findAll_empty_shouldComplete() {
        when(userRepository.findAll()).thenReturn(Flux.empty());
        StepVerifier.create(userUseCase.findAll()).verifyComplete();
    }

    @Test
    void findByIds_emptyList_shouldComplete() {
        StepVerifier.create(userUseCase.findByIds(List.of())).verifyComplete();
        verify(userRepository, never()).findAllById(any());
    }

    @Test
    void findByIds_withDuplicatesAndNulls_preservesOrderAndDuplicates() {
        // ids con duplicado y un null
        List<Long> ids = Arrays.asList(10L, 10L, null, 20L);

        // u1 y u2 ya están creados en setUp() con ids 10 y 20
        when(userRepository.findAllById(eq(List.of(10L, 20L)))).thenReturn(Flux.just(u1, u2));

        StepVerifier.create(userUseCase.findByIds(ids))
                // Debe respetar el orden original y repetir el duplicado
                .expectNext(u1)
                .expectNext(u1)
                .expectNext(u2)
                .verifyComplete();
    }

    @Test
    void findByIds_allMissing_shouldReturnEmptyAndHitEmptyBranch() {
        List<Long> ids = List.of(999L);

        // Devuelve vacío -> no encontró nada
        when(userRepository.findAllById(eq(ids))).thenReturn(Flux.empty());

        StepVerifier.create(userUseCase.findByIds(ids))
                .verifyComplete();

        // No afirmamos el mensaje exacto para no acoplar,
        // pero sí que hubo una advertencia o al menos un log.
        verify(logger, atLeastOnce()).warn(anyString(), any());
    }

}