package com.marcus.eventhub.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventHub API")
                        .description("REST API for creating, publishing, and joining events")
                        .version("0.2.0")
                        .contact(new Contact()
                                .name("EventHub")
                                .email("contato@eventhub.dev")))
                .servers(List.of(
                        new Server().url("/").description("Current host (use the same address you opened Swagger with)")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        Paste only the token returned by POST /auth/login \
                                        (do not include 'Bearer ' — Swagger adds it automatically).""")));
    }
}
