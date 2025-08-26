package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigInteger;
import java.time.LocalDate;

public record UserDTO(
        @NotBlank(message = "Los nombres son obligatorios") String nombres,
        @NotBlank(message = "Los apellidos son obligatorios") String apellidos,
        @Past(message = "La fecha nacimiento debe ser una fecha pasada") LocalDate fechaNacimiento,
        @NotBlank(message = "La direccion es obligatoria") String direccion,
        @NotBlank(message = "El telefono es obligatorio") String telefono,
        @Email(message = "El correo electronico es inválido") @NotBlank(message = "El correo electronico es obligatorio")
        String correoElectronico,
        @NotNull(message = "El salario base es obligatorio") @PositiveOrZero(message = "salario base debe ser mayor a 0") @Max(value = 15000000, message = "salario Base no puede exceder de 15000000") BigInteger salarioBase) {
}
