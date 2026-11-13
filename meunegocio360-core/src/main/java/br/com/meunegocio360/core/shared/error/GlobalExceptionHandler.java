package br.com.meunegocio360.core.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Único tratador de erros da API (RNF-BE-14). Nunca expõe stack trace, SQL ou nomes de classe
 * (RNF-024).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Problema> tratarApi(ApiException ex, HttpServletRequest request) {
    return responder(ex.tipo().status(), ex.cabecalhos(), Problemas.de(ex.tipo(), request));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problema> tratarValidacao(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<Problema.CampoInvalido> erros =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new Problema.CampoInvalido(e.getField(), e.getDefaultMessage()))
            .sorted(
                Comparator.comparing(Problema.CampoInvalido::field)
                    .thenComparing(Problema.CampoInvalido::message))
            .toList();
    return responder(
        HttpStatus.BAD_REQUEST,
        HttpHeaders.EMPTY,
        Problemas.de(TipoProblema.VALIDATION_ERROR, request, erros));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Problema> tratarCorpoInvalido(HttpServletRequest request) {
    List<Problema.CampoInvalido> erros =
        List.of(new Problema.CampoInvalido("corpo", "ausente ou com JSON malformado"));
    return responder(
        HttpStatus.BAD_REQUEST,
        HttpHeaders.EMPTY,
        Problemas.de(TipoProblema.VALIDATION_ERROR, request, erros));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problema> tratarInesperado(Exception ex, HttpServletRequest request) {
    if (ex instanceof ErrorResponse erro && !erro.getStatusCode().is5xxServerError()) {
      // Erros padrão do Spring MVC (404, 405, 415...), mantendo cabeçalhos como Allow.
      HttpStatus status = HttpStatus.valueOf(erro.getStatusCode().value());
      return responder(status, erro.getHeaders(), Problemas.generico(status, request));
    }
    log.error(
        "Erro inesperado ao processar {} {}", request.getMethod(), request.getRequestURI(), ex);
    return responder(
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpHeaders.EMPTY,
        Problemas.de(TipoProblema.INTERNAL_ERROR, request));
  }

  private ResponseEntity<Problema> responder(
      HttpStatus status, HttpHeaders cabecalhos, Problema problema) {
    return ResponseEntity.status(status)
        .headers(cabecalhos)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problema);
  }
}
