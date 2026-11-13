package br.com.meunegocio360.core.auth.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Parâmetros configuráveis do RF-BE-001 ({@code app.auth.*}). A aplicação falha na inicialização se
 * algum valor obrigatório estiver ausente ou inválido (RNF-BE-32).
 */
@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
    @Positive int maxTentativas,
    @Positive int bloqueioMinutos,
    @NotNull @Valid Jwt jwt,
    @NotNull @Valid Refresh refresh,
    @NotNull @Valid Cookie cookie) {

  public Duration duracaoBloqueio() {
    return Duration.ofMinutes(bloqueioMinutos);
  }

  /** RN-05 e seção "Token de acesso (JWT)". */
  public record Jwt(String secret, @NotBlank String issuer, @Positive int expiracaoMinutos) {

    private static final int TAMANHO_MINIMO_BYTES = 32;

    public Jwt {
      if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_BYTES) {
        throw new IllegalStateException(
            "JWT_SECRET ausente ou com menos de 32 bytes (256 bits). Gere com: openssl rand"
                + " -base64 48");
      }
    }

    public Duration validade() {
      return Duration.ofMinutes(expiracaoMinutos);
    }

    /** Não expõe o segredo em logs ou mensagens de erro. */
    @Override
    public String toString() {
      return "Jwt[secret=***, issuer=" + issuer + ", expiracaoMinutos=" + expiracaoMinutos + "]";
    }
  }

  /** RN-06 e rotina de limpeza. */
  public record Refresh(@Positive int expiracaoDias, @Positive int retencaoDias) {

    public Duration validade() {
      return Duration.ofDays(expiracaoDias);
    }

    public Duration retencao() {
      return Duration.ofDays(retencaoDias);
    }
  }

  /** Atributos configuráveis do cookie de refresh. */
  public record Cookie(boolean secure) {}
}
