package co.com.bancolombia.api.controller;

import co.com.bancolombia.model.exceptions.DuplicateEmailException;
import co.com.bancolombia.model.exceptions.NotFoundException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;   // <-- IMPORT NECESARIO
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*; // <-- usar JUnit puro
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoggerRepository logger;

    @InjectMocks
    private UserUseCase useCase;

    private User existing;
    private User toCreate;

    @BeforeEach
    void setUp() {
        existing = User.builder()
                .userId(100L)
                .name("Carol")
                .lastName("Velez")
                .document("12345")
                .birthDate(LocalDate.parse("1996-04-10"))
                .address("Cra 1 # 23-45")
                .phone("3172985404")
                .email("carol@example.com")
                .baseSalary(BigInteger.valueOf(2_500_000))
                .build();

        toCreate = User.builder()
                .name("Nuevo")
                .lastName("Usuario")
                .document("99999")
                .birthDate(LocalDate.parse("1990-01-01"))
                .address("Calle 1")
                .phone("3000000000")
                .email("nuevo@example.com")
                .baseSalary(BigInteger.valueOf(1_000_000))
                .build();
    }

    // -------- create(): éxito --------
    @Test
    void create_whenEmailNotExists_shouldSaveAndLog() {
        when(userRepository.existsByEmail("nuevo@example.com")).thenReturn(Mono.just(false));
        User saved = toCreate.toBuilder().userId(101L).build();
        when(userRepository.save(toCreate)).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.create(toCreate))
                .assertNext(u -> {
                    assertNotNull(u.getUserId());
                    assertEquals(101L, u.getUserId());
                    assertEquals("nuevo@example.com", u.getEmail());
                })
                .verifyComplete();

        verify(userRepository).existsByEmail("nuevo@example.com");
        verify(userRepository).save(toCreate);

        // Verifica las dos llamadas al logger (inicio y éxito)
        verify(logger).info(startsWith("Inicio creacion de usuario"), any());
        verify(logger).info(startsWith("Usuario creado userId="), any(), any());

        verifyNoMoreInteractions(logger);
    }

    // -------- create(): duplicado (error) --------
    @Test
    void create_whenEmailExists_shouldErrorWithDuplicateEmail() {
        when(userRepository.existsByEmail("carol@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.create(existing))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof DuplicateEmailException);
                    assertTrue(err.getMessage().contains("carol@example.com"));
                })
                .verify();

        verify(userRepository).existsByEmail("carol@example.com");
        verify(userRepository, never()).save(any());

        // Se debe loguear inicio y correo duplicado
        verify(logger).info(startsWith("Inicio creacion de usuario"), any());
        verify(logger).info(startsWith("Correo duplicado detectado"), any());

        verifyNoMoreInteractions(logger);
    }

    // -------- create(): save falla (error) --------
    @Test
    void create_whenSaveFails_shouldPropagateErrorAndNotLogSuccess() {
        when(userRepository.existsByEmail("nuevo@example.com")).thenReturn(Mono.just(false));
        RuntimeException boom = new RuntimeException("db down");
        when(userRepository.save(toCreate)).thenReturn(Mono.error(boom));

        StepVerifier.create(useCase.create(toCreate))
                .expectErrorMatches(e -> e == boom)
                .verify();

        verify(userRepository).existsByEmail("nuevo@example.com");
        verify(userRepository).save(toCreate);

        // Debe loguearse el inicio, pero NO el "Usuario creado..."
        verify(logger).info(startsWith("Inicio creacion de usuario"), any());
        verify(logger, never()).info(startsWith("Usuario creado userId="), any(), any());

        verifyNoMoreInteractions(logger);
    }

    // -------- findById(): éxito --------
    @Test
    void findById_whenExists_shouldReturnUser() {
        when(userRepository.findById(100L)).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.findById(100L))
                .expectNext(existing)
                .verifyComplete();

        verify(userRepository).findById(100L);
        verifyNoInteractions(logger);
    }

    // -------- findById(): no encontrado --------
    @Test
    void findById_whenEmpty_shouldErrorNotFound() {
        when(userRepository.findById(101L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findById(101L))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertTrue(err.getMessage().contains("Usuario no encontrado por Id: 101"));
                })
                .verify();

        verify(userRepository).findById(101L);
        verifyNoInteractions(logger);
    }

    // -------- findById(): repo lanza error --------
    @Test
    void findById_whenRepositoryErrors_shouldPropagateError() {
        RuntimeException boom = new RuntimeException("repo error");
        when(userRepository.findById(123L)).thenReturn(Mono.error(boom));

        StepVerifier.create(useCase.findById(123L))
                .expectErrorMatches(e -> e == boom)
                .verify();

        verify(userRepository).findById(123L);
        verifyNoInteractions(logger);
    }

    // -------- findAll(): lista --------
    @Test
    void findAll_shouldReturnFlux() {
        User other = existing.toBuilder().userId(200L).email("otro@example.com").build();
        when(userRepository.findAll()).thenReturn(Flux.just(existing, other));

        StepVerifier.create(useCase.findAll())
                .expectNext(existing)
                .expectNext(other)
                .verifyComplete();

        verify(userRepository).findAll();
        verifyNoInteractions(logger);
    }

    // -------- findAll(): vacío --------
    @Test
    void findAll_whenEmpty_shouldCompleteWithoutValues() {
        when(userRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findAll())
                .verifyComplete();

        verify(userRepository).findAll();
        verifyNoInteractions(logger);
    }

    @Test
    void create_whenValidationFails_shouldErrorBeforeRepository() {
        User bad = toCreate.toBuilder().email("mal-email").build();

        StepVerifier.create(useCase.create(bad))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(logger).info(startsWith("Inicio creacion de usuario"), any());
        verifyNoMoreInteractions(logger);
    }


    // ====== (Opcional) TESTS DE UPDATE: descomenta cuando implementes el caso de uso ======
    /*
    @Test
    void update_whenFound_shouldApplyChangesAndReturnUpdated() {
        when(userRepository.findById(100L)).thenReturn(Mono.just(existing));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        User updated = existing.toBuilder()
                .name("Nuevo")
                .lastName("Usuario")
                .birthDate(LocalDate.parse("1990-01-01"))
                .address("Calle 1")
                .phone("3000000000")
                .email("nuevo@example.com")
                .baseSalary(BigInteger.valueOf(1_000_000))
                .document("99999")
                .build();

        when(userRepository.update(any(User.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.update(100L, toCreate))
                .assertNext(u -> {
                    assertEquals("Nuevo", u.getName());
                    assertEquals("nuevo@example.com", u.getEmail());
                })
                .verifyComplete();

        verify(userRepository).findById(100L);
        verify(userRepository).update(captor.capture());
        User toUpdate = captor.getValue();

        assertEquals(100L, toUpdate.getUserId());
        assertEquals("Nuevo", toUpdate.getName());
        assertEquals("Usuario", toUpdate.getLastName());
        assertEquals(LocalDate.parse("1990-01-01"), toUpdate.getBirthDate());
        assertEquals("Calle 1", toUpdate.getAddress());
        assertEquals("3000000000", toUpdate.getPhone());
        assertEquals("nuevo@example.com", toUpdate.getEmail());
        assertEquals(BigInteger.valueOf(1_000_000), toUpdate.getBaseSalary());
        assertEquals("99999", toUpdate.getDocument());

        verifyNoInteractions(logger);
    }

    @Test
    void update_whenNotFound_shouldErrorNotFound() {
        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.update(999L, toCreate))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertTrue(err.getMessage().contains("Usuario no encontrado: 999"));
                })
                .verify();

        verify(userRepository).findById(999L);
        verify(userRepository, never()).update(any());
        verifyNoInteractions(logger);
    }
    */
}