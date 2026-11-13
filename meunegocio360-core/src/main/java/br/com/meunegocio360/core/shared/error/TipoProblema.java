package br.com.meunegocio360.core.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Catálogo de problemas da API (RNF-BE-14). O {@code code} é estável e em UPPER_SNAKE_CASE; o
 * cliente NÃO deve usar {@code title}/{@code detail} como lógica.
 */
public enum TipoProblema {
  VALIDATION_ERROR(
      HttpStatus.BAD_REQUEST, "validacao", "Dados inválidos", "Um ou mais campos são inválidos."),
  INVALID_CREDENTIALS(
      HttpStatus.UNAUTHORIZED,
      "credenciais-invalidas",
      "Credenciais inválidas",
      "E-mail ou senha inválidos."),
  INVALID_REFRESH_TOKEN(
      HttpStatus.UNAUTHORIZED,
      "refresh-token-invalido",
      "Sessão inválida",
      "Sessão expirada ou inválida. Entre novamente."),
  UNAUTHORIZED(
      HttpStatus.UNAUTHORIZED,
      "nao-autenticado",
      "Não autenticado",
      "Token de acesso ausente, inválido ou expirado."),
  ACCESS_DENIED(
      HttpStatus.FORBIDDEN,
      "acesso-negado",
      "Acesso negado",
      "Você não tem permissão para acessar este recurso."),
  ORIGIN_NOT_ALLOWED(
      HttpStatus.FORBIDDEN,
      "origem-nao-permitida",
      "Origem não permitida",
      "A origem da requisição não é permitida."),
  ACCOUNT_TEMPORARILY_LOCKED(
      HttpStatus.TOO_MANY_REQUESTS,
      "conta-bloqueada",
      "Conta temporariamente bloqueada",
      "Muitas tentativas de acesso. Tente novamente mais tarde."),
  INTERNAL_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "erro-interno",
      "Erro interno",
      "Ocorreu um erro inesperado. Tente novamente mais tarde.");

  private static final String BASE_URI = "https://meunegocio360.com.br/problemas/";

  private final HttpStatus status;
  private final String slug;
  private final String titulo;
  private final String detalhe;

  TipoProblema(HttpStatus status, String slug, String titulo, String detalhe) {
    this.status = status;
    this.slug = slug;
    this.titulo = titulo;
    this.detalhe = detalhe;
  }

  public HttpStatus status() {
    return status;
  }

  public String uri() {
    return BASE_URI + slug;
  }

  public String titulo() {
    return titulo;
  }

  public String detalhe() {
    return detalhe;
  }
}
