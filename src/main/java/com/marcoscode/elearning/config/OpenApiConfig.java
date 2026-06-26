package com.marcoscode.elearning.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI elearningOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return  new OpenAPI()
                .info(new Info()
                        .title("E-Learning REST API")
                        .version("1.0.0")
                        .description("""
                                REST API for the E-Learning Platform.
                                
                                Features:
                                - JWT Authentication
                                - Refresh Tokens
                                - Role-based Authorization
                                - Course Management
                                - Student Enrollment
                                - Instructor Management
                                """)

                )

                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                .schemaRequirement(
                        securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}
