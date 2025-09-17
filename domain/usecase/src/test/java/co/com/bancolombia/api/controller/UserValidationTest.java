package co.com.bancolombia.api.controller;

import co.com.bancolombia.model.exceptions.DuplicateEmailException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.userValidation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidationTest {

    @Mock private UserRepository userRepository;
    @Mock private LoggerRepository logger;

    private UserValidation userValidation;

    @BeforeEach
    void setUp() {
        userValidation = new UserValidation(userRepository, logger);
    }

    private User base() {
        return User.builder()
                .userId(1L)
                .name("Carol")
                .lastName("Velez")
                .email("carol@example.com")
                .password("secret123")
                .baseSalary(BigInteger.valueOf(3_000_000))
                .build();
    }


    @Test
    void validate_ok_shouldComplete() {
        StepVerifier.create(userValidation.validate(base()))
                .verifyComplete();
    }

    @Test
    void validate_invalidEmail_shouldThrow() {
        User u = base(); u.setEmail("bad-email");
        assertThrows(IllegalArgumentException.class, () -> userValidation.validate(u));
    }

    @Test
    void validate_negativeSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(BigInteger.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> userValidation.validate(u));
    }

    @Test
    void validate_aboveMaxSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(BigInteger.valueOf(15_000_001));
        assertThrows(IllegalArgumentException.class, () -> userValidation.validate(u));
    }

    @Test
    void validate_nullUser_shouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> userValidation.validate(null));
    }


    @Test
    void validateUserExists_duplicate_shouldError() {
        when(userRepository.existsByEmail("carol@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userValidation.validateUserExists("carol@example.com"))
                .expectError(DuplicateEmailException.class)
                .verify();

        verify(logger).info("Correo duplicado detectado: {}", "carol@example.com");
    }

    @Test
    void validateUserExists_available_shouldComplete() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(Mono.just(false));

        StepVerifier.create(userValidation.validateUserExists("new@example.com"))
                .verifyComplete();

        verify(logger, never()).info(anyString(), any());
    }

    @Test
    void validate_nullSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(null);
        assertThrows(IllegalArgumentException.class, () -> userValidation.validate(u));
    }

    @Test
    void validate_zeroSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(BigInteger.ZERO);
        assertThrows(IllegalArgumentException.class, () -> userValidation.validate(u));
    }

    @Test
    void validate_minPositiveSalary_shouldComplete() {
        User u = base(); u.setBaseSalary(BigInteger.ONE);
        userValidation.validate(u);
    }

    @Test
    void validate_maxSalaryBoundary_shouldComplete() {
        User u = base(); u.setBaseSalary(BigInteger.valueOf(15_000_000));
        userValidation.validate(u);
    }

    @Test
    void validate_blankEmail_shouldThrow() {
        User u = base(); u.setEmail("  ");
        assertThrows(IllegalArgumentException.class, () -> userValidation.validate(u));
    }

    @Test
    void validateUserExistsForDocument_duplicate_shouldError() {
        // Existe alguien con ese documento -> true
        when(userRepository.findByDocument(eq("123"))).thenReturn(Mono.just(true));

        StepVerifier.create(userValidation.validateUserExistsForDocument("123"))
                .expectErrorSatisfies(ex -> {
                    assert ex != null; // falla como se espera
                })
                .verify();

        verify(userRepository).findByDocument("123");
    }


    @Test
    void validateUserExistsForDocument_available_shouldComplete() {
        // No existe nadie con ese documento -> false
        when(userRepository.findByDocument(eq("ABC123"))).thenReturn(Mono.just(false));

        StepVerifier.create(userValidation.validateUserExistsForDocument("ABC123"))
                .verifyComplete();

        verify(userRepository).findByDocument("ABC123");
    }


}