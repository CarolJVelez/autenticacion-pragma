package co.com.bancolombia.api.dto.request;

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
public class UpdateUserDTO {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "La fecha nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La direccion es obligatoria")
    private String direccion;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    @Email(message = "El correo electronico es inválido")
    @NotBlank(message = "El correo electronico es obligatorio")
    private String correoElectronico;

    @NotNull(message = "El salario base es obligatorio")
    @PositiveOrZero(message = "salario base debe ser mayor a 0")
    @Max(value = 15000000, message = "salario Base no puede exceder de 15000000")
    private BigInteger salarioBase;
}


