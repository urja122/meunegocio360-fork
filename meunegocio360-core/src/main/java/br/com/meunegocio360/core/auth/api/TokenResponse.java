package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.SessaoEmitida;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de login e de renovação. O refresh token NUNCA aparece aqui: vai apenas no cookie {@code
 * HttpOnly} (RN-06).
 */
public record TokenResponse(
    @Schema(example = "Bearer") String tokenType,
    @Schema(example = "eyJhbGciOi...") String accessToken,
    @Schema(description = "Validade do token de acesso, em segundos", example = "900")
        long expiresIn,
    UsuarioResponse usuario) {

  static TokenResponse de(SessaoEmitida sessao) {
    return new TokenResponse(
        "Bearer",
        sessao.accessToken(),
        sessao.validadeAccessToken().toSeconds(),
        UsuarioResponse.de(sessao.usuario()));
  }

  /** Não expõe o token de acesso em logs. */
  @Override
  public String toString() {
    return "TokenResponse[tokenType="
        + tokenType
        + ", accessToken=***, expiresIn="
        + expiresIn
        + "]";
  }
}
