package co.com.bancolombia.r2dbc.reactiveRepositoryUser;

import co.com.bancolombia.model.rol.Rol;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.r2dbc.entities.UserEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import co.com.bancolombia.r2dbc.reactiveRepositoryRol.MyReactiveRepositoryRol;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User/* change for domain model */,
        UserEntity/* change for adapter model */,
        Long,
        MyReactiveRepository
> implements UserRepository {

    private final MyReactiveRepositoryRol rolRepository;
    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, MyReactiveRepositoryRol rolRepository) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.mapBuilder(d, User.UserBuilder.class).build());
        this.rolRepository = rolRepository;
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteByUserId(id);
    }

    @Override
    public Mono<User> update(User u) {
        return null;
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .flatMap(ue -> {
                    User user = this.toEntity(ue);
                    Long roleId = ue.getRoleId();
                    if (roleId == null) {
                        return Mono.just(user);
                    }
                    return rolRepository.findById(roleId)
                            .map(re -> {
                                Rol rol = Rol.builder()
                                        .roleId(re.getId())
                                        .name(re.getName())
                                        .description(re.getDescription())
                                        .build();
                                user.setRole(rol);
                                return user;
                            })
                            .defaultIfEmpty(user);
                });
    }

    @Override
    public Mono<User> save(User u) {
        if (u.getRole() == null || u.getRole().getRoleId() == null) {
            return Mono.error(new IllegalStateException("Rol sin id al guardar usuario"));
        }
        UserEntity data = mapper.map(u, UserEntity.class);
        data.setRoleId(u.getRole().getRoleId());
        return repository.save(data)
                .map(saved -> {
                    User out = this.toEntity(saved);
                    out.setRole(u.getRole());
                    return out;
                });
    }
}
