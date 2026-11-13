package br.com.meunegocio360.core.shared.security;

import br.com.meunegocio360.core.shared.error.ProblemaWriter;
import br.com.meunegocio360.core.shared.error.Problemas;
import br.com.meunegocio360.core.shared.error.TipoProblema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Responde 403 {@code ACCESS_DENIED} em {@code problem+json}. */
@Component
public class ProblemaAccessDeniedHandler implements AccessDeniedHandler {

  private final ProblemaWriter problemaWriter;

  public ProblemaAccessDeniedHandler(ProblemaWriter problemaWriter) {
    this.problemaWriter = problemaWriter;
  }

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {
    problemaWriter.escrever(response, Problemas.de(TipoProblema.ACCESS_DENIED, request));
  }
}
