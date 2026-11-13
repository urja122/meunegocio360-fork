package br.com.meunegocio360.core.shared.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origens permitidas ({@code CORS_ALLOWED_ORIGINS}), usadas no CORS (RNF-BE-42) e na verificação de
 * {@code Origin} dos endpoints com cookie (RNF-BE-40). Falha na inicialização se vazia ou com
 * curinga (RNF-BE-32).
 */
@ConfigurationProperties(prefix = "app.cors")
public record OrigensPermitidasProperties(List<String> allowedOrigins) {

  public OrigensPermitidasProperties {
    allowedOrigins =
        allowedOrigins == null
            ? List.of()
            : allowedOrigins.stream().map(String::trim).filter(o -> !o.isEmpty()).toList();
    if (allowedOrigins.isEmpty()) {
      throw new IllegalStateException(
          "CORS_ALLOWED_ORIGINS deve conter ao menos uma origem (ex.: http://localhost:5173).");
    }
    if (allowedOrigins.stream().anyMatch(o -> o.contains("*"))) {
      throw new IllegalStateException("CORS_ALLOWED_ORIGINS não pode conter curinga (*).");
    }
  }

  public boolean permite(String origem) {
    return allowedOrigins.contains(origem);
  }
}
