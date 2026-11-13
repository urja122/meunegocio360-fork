package br.com.meunegocio360.core.shared.security;

import br.com.meunegocio360.core.shared.error.ProblemaWriter;
import br.com.meunegocio360.core.shared.error.Problemas;
import br.com.meunegocio360.core.shared.error.TipoProblema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Responde 401 {@code UNAUTHORIZED} em {@code problem+json} com {@code WWW-Authenticate: Bearer}
 * quando o token de acesso está ausente, inválido ou expirado.
 */
@Component
public class ProblemaAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ProblemaWriter problemaWriter;

  public ProblemaAuthenticationEntryPoint(ProblemaWriter problemaWriter) {
    this.problemaWriter = problemaWriter;
  }

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {
    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    problemaWriter.escrever(response, Problemas.de(TipoProblema.UNAUTHORIZED, request));
  }
}
