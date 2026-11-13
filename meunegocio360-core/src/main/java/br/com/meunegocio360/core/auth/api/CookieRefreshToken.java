package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthProperties;
import java.time.Duration;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Monta o cookie {@code mn360_refresh}: {@code HttpOnly}, {@code SameSite=Strict}, {@code
 * Path=/api/v1/auth}, {@code Secure} configurável e {@code Max-Age} igual à validade do refresh
 * token (seção "Cookie de refresh" do RF-BE-001).
 */
@Component
class CookieRefreshToken {

  static final String NOME = "mn360_refresh";
  private static final String CAMINHO = "/api/v1/auth";

  private final boolean secure;

  CookieRefreshToken(AuthProperties properties, Environment environment) {
    this.secure = properties.cookie().secure();
    if (!secure && !environment.matchesProfiles("dev")) {
      throw new IllegalStateException("APP_AUTH_COOKIE_SECURE=false só é permitido no perfil dev.");
    }
  }

  String criar(String refreshToken, Duration validade) {
    return base(refreshToken).maxAge(validade).build().toString();
  }

  /** Cookie vazio com {@code Max-Age=0}, que o navegador remove imediatamente. */
  String expirar() {
    return base("").maxAge(Duration.ZERO).build().toString();
  }

  private ResponseCookie.ResponseCookieBuilder base(String valor) {
    return ResponseCookie.from(NOME, valor)
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path(CAMINHO);
  }
}
