package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.shared.error.ApiException;
import br.com.meunegocio360.core.shared.error.TipoProblema;
import org.springframework.http.HttpHeaders;

/** Token válido, mas o usuário não existe mais ou foi desativado. */
public class UsuarioNaoAutenticadoException extends ApiException {

  public UsuarioNaoAutenticadoException() {
    super(TipoProblema.UNAUTHORIZED);
    comCabecalho(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
  }
}
