package co.com.bancolombia.api.dto.response;

import java.math.BigInteger;

public record UserResponseDTO(
        String userId,
        String nombres,
        String apellidos,
        java.time.LocalDate fechaNacimiento,
        String direccion,
        String telefono,
        String correoElectronico,
        BigInteger salarioBase
) {}
