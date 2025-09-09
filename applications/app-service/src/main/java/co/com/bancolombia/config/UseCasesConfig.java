package co.com.bancolombia.config;

import co.com.bancolombia.model.rol.gateways.RolRepository;
import co.com.bancolombia.model.security.gateways.PasswordEncoderGateway;
import co.com.bancolombia.model.security.gateways.TokenProvider;
import co.com.bancolombia.model.user.gateways.LoggerRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.auth.AuthUseCase;
import co.com.bancolombia.usecase.user.UserUseCase;
import co.com.bancolombia.usecase.userValidation.UserValidation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
public class UseCasesConfig {

    @Bean
    public UserValidation userValidation(
            UserRepository userRepository,
            LoggerRepository logger
    ) {
        return new UserValidation(userRepository, logger);
    }

    @Bean
    public UserUseCase userUseCase(
            UserRepository userRepository,
            LoggerRepository logger,
            PasswordEncoderGateway passwordEncoder,
            UserValidation userValidation,
            RolRepository rolRepository
    ) {
        return new UserUseCase(userRepository, logger, passwordEncoder, userValidation, rolRepository);
    }

    @Bean
    public AuthUseCase authUseCase(
            UserRepository userRepository,
            PasswordEncoderGateway passwordEncoder,
            TokenProvider tokenProvider,
            LoggerRepository logger
    ) {
        return new AuthUseCase(userRepository, passwordEncoder, tokenProvider, logger);
    }


}
