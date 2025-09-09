package co.com.bancolombia.api.dto.request;

import co.com.bancolombia.model.rol.Rol;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {

    @NotBlank(message = "Los nombres son obligatorios")
    private String name;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String lastName;

    @NotBlank(message = "El documento de identidad es obligatorio")
    private String document;

    @NotBlank(message = "La Contraseña es obligatoria")
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    private String role; // ADMIN, ASESOR, CLIENTE

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "La fecha nacimiento debe ser una fecha pasada")
    private LocalDate birthDate;

    @NotBlank(message = "La direccion es obligatoria")
    private String address;

    @NotBlank(message = "El telefono es obligatorio")
    private String phone;

    @Email(message = "El correo electronico es inválido")
    @NotBlank(message = "El correo electronico es obligatorio")
    private String email;

    @NotNull(message = "El salario base es obligatorio")
    @PositiveOrZero(message = "salario base debe ser mayor a 0")
    @Max(value = 15000000, message = "salario Base no puede exceder de 15000000")
    private BigInteger baseSalary;
}
