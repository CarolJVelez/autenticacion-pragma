package co.com.bancolombia.model.rol;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Rol {
    private Long roleId;
    private String name; // ADMIN, ASESOR, CLIENTE
    private String description;

}
