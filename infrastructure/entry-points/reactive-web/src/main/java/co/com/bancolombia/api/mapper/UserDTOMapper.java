package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.CreateUserDTO;
import co.com.bancolombia.api.dto.request.UpdateUserDTO;
import co.com.bancolombia.api.dto.request.UserDTO;
import co.com.bancolombia.api.dto.response.UserResponseDTO;
import co.com.bancolombia.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    User toDomain(UserDTO dto);

    UserDTO toDto(User user);

    List<UserDTO> toResponseList(List<User> users);

    @Mappings({
            @Mapping(target = "name",        source = "nombres"),
            @Mapping(target = "lastname",    source = "apellidos"),
            @Mapping(target = "birthDate",   source = "fechaNacimiento"),
            @Mapping(target = "address",     source = "direccion"),
            @Mapping(target = "phone",       source = "telefono"),
            @Mapping(target = "email",       source = "correoElectronico"),
            @Mapping(target = "baseSalary",  source = "salarioBase")
    })
    User toModel(CreateUserDTO user);

    @Mappings({
            @Mapping(target = "name",        source = "nombres"),
            @Mapping(target = "lastname",    source = "apellidos"),
            @Mapping(target = "birthDate",   source = "fechaNacimiento"),
            @Mapping(target = "address",     source = "direccion"),
            @Mapping(target = "phone",       source = "telefono"),
            @Mapping(target = "email",       source = "correoElectronico"),
            @Mapping(target = "baseSalary",  source = "salarioBase")
    })
    User toModel(UpdateUserDTO userDTO);

    UserResponseDTO toResponse(User user);
}
