package co.com.bancolombia.usecase.userValidation;

import co.com.bancolombia.model.exceptions.DuplicateEmailException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class UserValidation {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final BigDecimal SALARIO_MAX = new BigDecimal("15000000");

    private final UserRepository userRepository;

    private final LoggerRepository logger;

    public UserValidation(UserRepository userRepository, LoggerRepository logger) {
        this.userRepository = userRepository;
        this.logger = logger;
    }

    public Mono<Void> validate(User user) {

        if (isBlank(user.getEmail()) || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Correo electrónico inválido");
        }

        BigDecimal salario = toBigDecimal(user.getBaseSalary());
        if (salario == null
                || salario.compareTo(BigDecimal.ZERO) <= 0
                || salario.compareTo(SALARIO_MAX) > 0) {
            throw new IllegalArgumentException("El salario base debe estar entre 0 y 15,000,000");
        }
        return Mono.empty();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try { return new BigDecimal(value.toString()); } catch (Exception e) { return null; }
    }

    public Mono<Void> validateUserExists(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(exist -> {
                    if (exist) {
                        logger.info("Correo duplicado detectado: {}", email);
                        return Mono.error(new DuplicateEmailException(email));
                    }
                    return Mono.empty();
                });
    }

}
