package co.com.bancolombia.r2dbc.reactiveRepositoryRol;

import co.com.bancolombia.model.exceptions.NotFoundException;
import co.com.bancolombia.model.rol.Rol;
import co.com.bancolombia.model.rol.gateways.RolRepository;
import co.com.bancolombia.r2dbc.entities.RolEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public class MyReactiveRepositoryAdapterRol extends ReactiveAdapterOperations<
        Rol/* change for domain model */,
        RolEntity/* change for adapter model */,
        Long,
        MyReactiveRepositoryRol
> implements RolRepository {
    public MyReactiveRepositoryAdapterRol(MyReactiveRepositoryRol repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.mapBuilder(d, Rol.RolBuilder.class).build());
    }

    private Rol toModel(RolEntity re) {
        return Rol.builder()
                .roleId(re.getId())
                .name(re.getName())
                .description(re.getDescription())
                .build();
    }

    @Override
    public Mono<Rol> findById(Long id) {
        return repository.findById(id).map(this::toModel);
    }

    @Override
    public Mono<Rol> findByName(String name) {
        return repository.findByName(name)
                .map(this::toModel);
    }

}
