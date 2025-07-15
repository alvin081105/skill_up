package com.gbsw.gbsw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gbsw API 문서")
                        .description("JWT 인증 포함 Swagger 설정")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .name("Bearer Authentication")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
