package br.com.meunegocio360.core.shared.error;

import br.com.meunegocio360.core.shared.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

/** Fábrica do corpo {@link Problema}, preenchendo {@code instance} e {@code traceId}. */
public final class Problemas {

  private Problemas() {}

  public static Problema de(TipoProblema tipo, HttpServletRequest request) {
    return de(tipo, request, null);
  }

  public static Problema de(
      TipoProblema tipo, HttpServletRequest request, List<Problema.CampoInvalido> erros) {
    return new Problema(
        tipo.uri(),
        tipo.titulo(),
        tipo.status().value(),
        tipo.detalhe(),
        request.getRequestURI(),
        tipo.name(),
        MDC.get(RequestIdFilter.MDC_CHAVE),
        erros);
  }

  /** Problema genérico para erros do Spring MVC sem código próprio (404, 405, 415...). */
  public static Problema generico(HttpStatus status, HttpServletRequest request) {
    return new Problema(
        "about:blank",
        status.getReasonPhrase(),
        status.value(),
        status.getReasonPhrase(),
        request.getRequestURI(),
        status.name(),
        MDC.get(RequestIdFilter.MDC_CHAVE),
        null);
  }
}
