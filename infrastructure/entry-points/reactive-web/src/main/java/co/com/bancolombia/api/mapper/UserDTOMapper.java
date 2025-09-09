package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.CreateUserDTO;
import co.com.bancolombia.api.dto.response.UserResponseDTO;
import co.com.bancolombia.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { RoleDTOMapper.class })
public interface UserDTOMapper {

    // CreateUserDTO.role (String) -> User.role (Rol) lo resuelve RoleDTOMapper.map(String)
    @Mapping(target = "userId", ignore = true) // <-- clave
    @Mapping(target = "role", source = "role") // usa RoleDTOMapper.map(String)
    User toModel(CreateUserDTO dto);

    // Y para respuesta: User.role:Rol -> UserResponseDTO.role:String usando RoleDTOMapper.map(Rol)
    UserResponseDTO toDto(User user);
}
