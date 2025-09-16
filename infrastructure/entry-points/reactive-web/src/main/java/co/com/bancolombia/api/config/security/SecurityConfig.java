package co.com.bancolombia.api.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JwtReactiveAuthenticationManager authenticationManager,
            BearerTokenServerAuthenticationConverter bearerConverter) {

        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authenticationManager);
        jwtFilter.setServerAuthenticationConverter(bearerConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/actuator/**", "/h2/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/usuarios/email/**").permitAll() // <-- agrega esto
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAnyRole("ADMIN", "ASESOR")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((exchange, ex) -> { // 401
                            var res = exchange.getResponse();
                            res.setStatusCode(HttpStatus.UNAUTHORIZED);
                            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            var json = """
                                    {"status":401,"message":"Token inválido o ausente."}
                                    """;
                            var buf = res.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
                            return res.writeWith(Mono.just(buf));
                        })
                        .accessDeniedHandler((exchange, ex) -> {      // 403
                            var res = exchange.getResponse();
                            res.setStatusCode(HttpStatus.FORBIDDEN);
                            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            var json = """
                                    {"status":403,"message":"No autorizado."}
                                    """;
                            var buf = res.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
                            return res.writeWith(Mono.just(buf));
                        })
                )
                .build();
    }
}