package co.com.bancolombia.usecase.auth;

import co.com.bancolombia.model.exceptions.InvalidCredentialsException;
import co.com.bancolombia.model.security.gateways.PasswordEncoderGateway;
import co.com.bancolombia.model.security.gateways.TokenProvider;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenProvider tokenProvider;
    private final LoggerRepository logger;

    public Mono<AuthResult> login(String email, String password) {
        logger.info("Intento de login para email={}", email);
        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new InvalidCredentialsException("Credenciales inválidas")))
            .flatMap(user -> passwordEncoder.matches(password, user.getPassword())
                .flatMap(match -> {
                    if (!match) return Mono.error(new InvalidCredentialsException("Credenciales inválidas"+ user.getPassword() + user.getName() + user.getName()));
                    System.out.println("carolv usuario role.." + user.getRole());
                    return tokenProvider.generateToken(user)
                            .zipWith(tokenProvider.getExpirationSeconds(),
                                (tok, exp) -> new AuthResult(tok, "Bearer", exp));
                }));
    }

    @Data
    @AllArgsConstructor
    public static class AuthResult {
        private String token;
        private String tokenType;
        private long expiresIn;
    }
}
