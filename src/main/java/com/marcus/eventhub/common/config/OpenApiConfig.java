package com.marcus.eventhub.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventHub API")
                        .description("API REST para criação, divulgação e participação em eventos")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("EventHub")
                                .email("contato@eventhub.dev")));
    }
}
