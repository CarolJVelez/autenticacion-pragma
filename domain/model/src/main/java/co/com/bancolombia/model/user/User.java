package co.com.bancolombia.model.user;

import co.com.bancolombia.model.rol.Rol;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private Long userId;
    private String name;
    private String lastName;
    private String password;
    private String document;
    private LocalDate birthDate;
    private String address;
    private String email;
    private String phone;
    private BigInteger baseSalary;
    private BigDecimal maxIndebtedness;
    private Rol role;
}
