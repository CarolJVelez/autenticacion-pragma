package co.com.bancolombia.api.mapper;

import co.com.bancolombia.model.rol.Rol;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleDTOMapper {

    default Rol map(String roleName) {
        if (roleName == null) return null;
        return Rol.builder()
                .name(roleName.trim().toUpperCase())   // ADMIN | ASESOR | CLIENTE
                .build();
    }

    // Rol (dominio) -> String (DTO)
    default String map(Rol rol) {
        return rol != null ? rol.getName() : null;
    }
}