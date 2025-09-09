package co.com.bancolombia.api.controller;

import co.com.bancolombia.model.exceptions.InvalidCredentialsException;
import co.com.bancolombia.model.security.gateways.PasswordEncoderGateway;
import co.com.bancolombia.model.security.gateways.TokenProvider;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.auth.AuthUseCase;
import co.com.bancolombia.usecase.auth.AuthUseCase.AuthResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoderGateway passwordEncoder;
    @Mock private TokenProvider tokenProvider;
    @Mock private LoggerRepository logger;

    @InjectMocks
    private AuthUseCase authUseCase;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .name("Carol")
                .lastName("Velez")
                .email("carol@example.com")
                .password("$2a$10$encoded") // hash almacenado
                .baseSalary(BigInteger.valueOf(5_000_000))
                .build();
    }

    @Test
    void login_ok_shouldReturnTokenAndExpiry() {
        String raw = "plaintext";
        when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(eq(raw), eq(user.getPassword()))).thenReturn(Mono.just(true));
        when(tokenProvider.generateToken(eq(user))).thenReturn(Mono.just("jwt-token"));
        when(tokenProvider.getExpirationSeconds()).thenReturn(Mono.just(3600L));

        StepVerifier.create(authUseCase.login(user.getEmail(), raw))
                .assertNext((AuthResult res) -> {
                    assertEquals("jwt-token", res.getToken());
                    assertEquals("Bearer", res.getTokenType());
                    assertEquals(3600L, res.getExpiresIn());
                })
                .verifyComplete();
    }

    @Test
    void login_wrongPassword_shouldErrorInvalidCredentials() {
        String wrong = "badpass";
        when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(eq(wrong), eq(user.getPassword()))).thenReturn(Mono.just(false));

        StepVerifier.create(authUseCase.login(user.getEmail(), wrong))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void login_userNotFound_shouldErrorInvalidCredentials() {
        when(userRepository.findByEmail(eq("missing@example.com"))).thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.login("missing@example.com", "whatever"))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void login_generateTokenFails_shouldPropagate() {
        String raw = "plaintext";
        when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(eq(raw), eq(user.getPassword()))).thenReturn(Mono.just(true));

        when(tokenProvider.getExpirationSeconds()).thenReturn(Mono.just(3600L));
        when(tokenProvider.generateToken(eq(user))).thenReturn(Mono.error(new RuntimeException("jwt-fail")));

        StepVerifier.create(authUseCase.login(user.getEmail(), raw))
                .expectErrorSatisfies(ex ->
                        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("jwt-fail")))
                .verify();
    }

    @Test
    void login_getExpirationFails_shouldPropagate() {
        String raw = "plaintext";
        when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(eq(raw), eq(user.getPassword()))).thenReturn(Mono.just(true));
        when(tokenProvider.generateToken(eq(user))).thenReturn(Mono.just("jwt-token"));
        when(tokenProvider.getExpirationSeconds()).thenReturn(Mono.error(new RuntimeException("exp-fail")));

        StepVerifier.create(authUseCase.login(user.getEmail(), raw))
                .expectErrorSatisfies(ex ->
                        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("exp-fail")))
                .verify();
    }

}