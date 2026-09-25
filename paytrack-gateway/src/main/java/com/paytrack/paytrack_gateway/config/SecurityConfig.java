package com.paytrack.paytrack_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Bean
  public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(ex -> ex
            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/webjars/**",
                          "/payment-service/swagger-ui/**",
                          "/payment-service/v3/api-docs/**", "/v3/api-docs/**").permitAll()
            .anyExchange().authenticated())
        .oauth2ResourceServer(server -> server.jwt(Customizer.withDefaults()))
        .build();
  }
}
