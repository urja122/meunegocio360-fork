package br.com.meunegocio360.core.shared.security;

import br.com.meunegocio360.core.shared.error.ProblemaWriter;
import br.com.meunegocio360.core.shared.error.Problemas;
import br.com.meunegocio360.core.shared.error.TipoProblema;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejeita com 403 {@code ORIGIN_NOT_ALLOWED} as requisições aos endpoints que usam cookie cujo
 * cabeçalho {@code Origin} esteja presente e fora da lista permitida (RNF-BE-40). Fica antes do
 * filtro de CORS para que a resposta siga o formato {@code problem+json} e nenhum token seja
 * rotacionado.
 */
public class VerificacaoOrigemFilter extends OncePerRequestFilter {

  private final RequestMatcher endpointsComCookie;
  private final OrigensPermitidasProperties origens;
  private final ProblemaWriter problemaWriter;

  public VerificacaoOrigemFilter(
      RequestMatcher endpointsComCookie,
      OrigensPermitidasProperties origens,
      ProblemaWriter problemaWriter) {
    this.endpointsComCookie = endpointsComCookie;
    this.origens = origens;
    this.problemaWriter = problemaWriter;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !endpointsComCookie.matches(request);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String origem = request.getHeader(HttpHeaders.ORIGIN);
    if (origem != null && !origens.permite(origem)) {
      problemaWriter.escrever(response, Problemas.de(TipoProblema.ORIGIN_NOT_ALLOWED, request));
      return;
    }
    chain.doFilter(request, response);
  }
}
