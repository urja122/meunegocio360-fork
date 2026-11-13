package br.com.meunegocio360.core.auth.application;

import java.time.Duration;

/**
 * Resultado de login ou renovação. O {@code refreshToken} em claro só existe aqui, para ser gravado
 * no cookie; nunca vai ao corpo da resposta nem ao banco (RN-06, RN-09).
 */
public record SessaoEmitida(
    String accessToken,
    Duration validadeAccessToken,
    String refreshToken,
    Duration validadeRefreshToken,
    UsuarioAutenticado usuario) {

  /** Evita expor os tokens em logs. */
  @Override
  public String toString() {
    return "SessaoEmitida[usuarioId=" + usuario.id() + "]";
  }
}
