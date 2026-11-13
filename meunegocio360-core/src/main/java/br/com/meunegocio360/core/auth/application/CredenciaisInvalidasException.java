package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.shared.error.ApiException;
import br.com.meunegocio360.core.shared.error.TipoProblema;

/**
 * RN-03: resposta única para e-mail inexistente, senha incorreta ou usuário inativo, impedindo a
 * enumeração de usuários.
 */
public class CredenciaisInvalidasException extends ApiException {

  public CredenciaisInvalidasException() {
    super(TipoProblema.INVALID_CREDENTIALS);
  }
}
