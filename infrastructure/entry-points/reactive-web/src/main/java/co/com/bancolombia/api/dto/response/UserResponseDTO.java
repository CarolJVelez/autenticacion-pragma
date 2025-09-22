package co.com.bancolombia.api.dto.response;

import co.com.bancolombia.model.rol.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String name;
    private String lastName;
    private String document;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String email;
    private BigInteger baseSalary;
    private BigDecimal maxIndebtedness;
    private Rol role;
}
