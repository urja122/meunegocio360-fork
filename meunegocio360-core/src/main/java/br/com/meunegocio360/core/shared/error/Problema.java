package br.com.meunegocio360.core.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Corpo padrão de erro em {@code application/problem+json} (RFC 9457), conforme RNF-BE-14.
 *
 * <p>{@code code} é o identificador estável usado pelo frontend; {@code detail} e {@code message}
 * são apenas textos para pessoas.
 */
@Schema(name = "Problema", description = "Erro no formato application/problem+json (RFC 9457)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Problema(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    String code,
    String traceId,
    List<CampoInvalido> errors) {

  /** Campo inválido em erros de validação. */
  public record CampoInvalido(String field, String message) {}
}
