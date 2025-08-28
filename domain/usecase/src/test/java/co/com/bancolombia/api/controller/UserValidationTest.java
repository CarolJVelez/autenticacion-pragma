package co.com.bancolombia.api.controller;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.usecase.userValidation.UserValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private User base() {
        return User.builder()
                .name("Carol")
                .lastName("Velez")
                .document("123")
                .birthDate(LocalDate.parse("1996-04-10"))
                .address("Cra 1")
                .phone("3000000000")
                .email("carol@example.com")
                .baseSalary(BigInteger.valueOf(1_000_000))
                .build();
    }

    @Test
    void validate_validUser_shouldPass() {
        assertDoesNotThrow(() -> UserValidation.validate(base()));
    }

    @Test
    void validate_nullUser_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> UserValidation.validate(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", " ",               // vacío o espacios
            "noatsign.com",        // sin @
            "@domain.com",         // local-part vacío
            "user@",               // domain vacío
            " user@domain.com",    // espacio inicial
            "user@domain.com ",    // espacio final
            "user@@domain.com",    // doble @
            "userdomain.com"       // sin @
    })
    void validate_badEmail_shouldThrow(String email) {
        User u = base(); u.setEmail(email);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> UserValidation.validate(u));
        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a@b",
            "bad@domain",
            "user.name+tag@sub-domain",
            "u_s-e.r@dom-ain"
    })
    void validate_emailsAcceptedByCurrentRegex_shouldPass(String email) {
        User u = base(); u.setEmail(email);
        assertDoesNotThrow(() -> UserValidation.validate(u));
    }

    @Test
    void validate_nullSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(null);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> UserValidation.validate(u));
        assertTrue(ex.getMessage().toLowerCase().contains("salario"));
    }

    @Test
    void validate_zeroSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(BigInteger.ZERO);
        assertThrows(IllegalArgumentException.class, () -> UserValidation.validate(u));
    }

    @Test
    void validate_negativeSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(BigInteger.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> UserValidation.validate(u));
    }

    @Test
    void validate_aboveMaxSalary_shouldThrow() {
        User u = base(); u.setBaseSalary(BigInteger.valueOf(15_000_001));
        assertThrows(IllegalArgumentException.class, () -> UserValidation.validate(u));
    }

    @Test
    void validate_nullUser_shouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> UserValidation.validate(null));
    }
}