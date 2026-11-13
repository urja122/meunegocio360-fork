package br.com.meunegocio360.core.shared.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Escreve um {@link Problema} diretamente na resposta. Usado por filtros e pelos handlers do Spring
 * Security, que atuam antes do {@code DispatcherServlet} e, portanto, fora do {@link
 * GlobalExceptionHandler}.
 */
@Component
public class ProblemaWriter {

  private final JsonMapper jsonMapper;

  public ProblemaWriter(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public void escrever(HttpServletResponse response, Problema problema) throws IOException {
    response.setStatus(problema.status());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    jsonMapper.writeValue(response.getOutputStream(), problema);
  }
}
