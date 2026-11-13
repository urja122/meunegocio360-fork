package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.shared.error.ApiException;
import br.com.meunegocio360.core.shared.error.TipoProblema;

/**
 * Refresh token ausente, desconhecido, expirado, revogado (reuso) ou de usuário inativo/bloqueado.
 */
public class RefreshTokenInvalidoException extends ApiException {

  public RefreshTokenInvalidoException() {
    super(TipoProblema.INVALID_REFRESH_TOKEN);
  }
}
