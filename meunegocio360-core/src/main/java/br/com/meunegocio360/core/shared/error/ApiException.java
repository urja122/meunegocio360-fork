package br.com.meunegocio360.core.shared.error;

import org.springframework.http.HttpHeaders;

/**
 * Exceção base convertida em {@code problem+json} pelo {@link GlobalExceptionHandler}. Pode
 * carregar cabeçalhos extras da resposta (por exemplo, {@code Retry-After} ou {@code Set-Cookie}).
 */
public abstract class ApiException extends RuntimeException {

  private final TipoProblema tipo;
  private final transient HttpHeaders cabecalhos = new HttpHeaders();

  protected ApiException(TipoProblema tipo) {
    super(tipo.name());
    this.tipo = tipo;
  }

  public TipoProblema tipo() {
    return tipo;
  }

  public HttpHeaders cabecalhos() {
    return HttpHeaders.readOnlyHttpHeaders(cabecalhos);
  }

  public ApiException comCabecalho(String nome, String valor) {
    cabecalhos.add(nome, valor);
    return this;
  }
}
