package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.shared.error.ApiException;
import br.com.meunegocio360.core.shared.error.TipoProblema;
import java.time.Duration;
import org.springframework.http.HttpHeaders;

/** RN-04: conta bloqueada por excesso de tentativas; informa {@code Retry-After} em segundos. */
public class ContaBloqueadaException extends ApiException {

  public ContaBloqueadaException(Duration tempoRestante) {
    super(TipoProblema.ACCOUNT_TEMPORARILY_LOCKED);
    long segundos = Math.max(1, (tempoRestante.toMillis() + 999) / 1000);
    comCabecalho(HttpHeaders.RETRY_AFTER, Long.toString(segundos));
  }
}
