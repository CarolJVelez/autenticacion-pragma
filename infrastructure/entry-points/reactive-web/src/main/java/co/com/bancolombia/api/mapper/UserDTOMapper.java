package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.CreateUserDTO;
import co.com.bancolombia.api.dto.response.UserResponseDTO;
import co.com.bancolombia.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    @Mapping(target = "name", source = "nombres")
    @Mapping(target = "lastname", source = "apellidos")
    @Mapping(target = "birthDate", source = "fechaNacimiento")
    @Mapping(target = "address", source = "direccion")
    @Mapping(target = "phone", source = "telefono")
    @Mapping(target = "email", source = "correoElectronico")
    @Mapping(target = "baseSalary", source = "salarioBase")
    User toModel(CreateUserDTO dto);

    @Mapping(target = "nombres", source = "name")
    @Mapping(target = "apellidos", source = "lastname")
    @Mapping(target = "fechaNacimiento", source = "birthDate")
    @Mapping(target = "direccion", source = "address")
    @Mapping(target = "telefono", source = "phone")
    @Mapping(target = "correoElectronico", source = "email")
    @Mapping(target = "salarioBase", source = "baseSalary")
    UserResponseDTO toDto(User user);
}
