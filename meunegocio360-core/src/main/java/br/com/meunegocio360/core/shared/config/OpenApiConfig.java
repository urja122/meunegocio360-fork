package br.com.meunegocio360.core.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados do OpenAPI e esquema de autenticação Bearer (RNF-030). */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

  public static final String ESQUEMA_BEARER = "bearerAuth";

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("MeuNegocio360 — Núcleo")
                .version("v1")
                .description("API do serviço Núcleo (identidade, autenticação e cadastros)."))
        .components(
            new Components()
                .addSecuritySchemes(
                    ESQUEMA_BEARER,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
