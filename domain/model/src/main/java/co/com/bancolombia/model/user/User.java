package co.com.bancolombia.model.user;

import lombok.*;

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
    private String document;
    private LocalDate birthDate;
    private String address;
    private String email;
    private String phone;
    private BigInteger baseSalary;
}
