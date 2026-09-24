package com.paytrack.paytrackpaymentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paytrackOpenAPI() {

        return new OpenAPI().info(new Info()
                .title("PayTrack Payment Service API")
                .description("Comptes, paiements et cartes bancaires de la plateforme PayTrack.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("PayTrack Engineering")
                        .email("dev@paytrack.io"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://paytrack.io")));
    }
}