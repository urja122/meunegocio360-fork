package br.com.meunegocio360.core.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lê ou gera o {@code X-Request-Id}, coloca-o no MDC dos logs e o devolve na resposta (RNF-BE-43,
 * RNF-050). Roda antes da cadeia do Spring Security para que erros 401/403 também tenham {@code
 * traceId}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String CABECALHO = "X-Request-Id";
  public static final String MDC_CHAVE = "requestId";

  /** Aceita apenas valores curtos e seguros, evitando injeção de conteúdo nos logs. */
  private static final Pattern FORMATO_VALIDO = Pattern.compile("[A-Za-z0-9._-]{1,64}");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String requestId = request.getHeader(CABECALHO);
    if (requestId == null || !FORMATO_VALIDO.matcher(requestId).matches()) {
      requestId = UUID.randomUUID().toString();
    }
    MDC.put(MDC_CHAVE, requestId);
    response.setHeader(CABECALHO, requestId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_CHAVE);
    }
  }
}
