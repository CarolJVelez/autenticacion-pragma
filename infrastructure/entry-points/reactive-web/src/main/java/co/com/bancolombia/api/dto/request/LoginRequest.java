package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "El correo electronico es inválido")
    @NotBlank(message = "El correo electronico es obligatorio")
    private String email;
    @NotBlank(message = "La Contraseña es obligatoria")
    @Size(min = 6, max = 100)
    private String password;
}
