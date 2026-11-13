package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AutenticarUsuario;
import br.com.meunegocio360.core.auth.application.ConsultarUsuarioAutenticado;
import br.com.meunegocio360.core.auth.application.EncerrarSessao;
import br.com.meunegocio360.core.auth.application.RefreshTokenInvalidoException;
import br.com.meunegocio360.core.auth.application.RenovarSessao;
import br.com.meunegocio360.core.auth.application.SessaoEmitida;
import br.com.meunegocio360.core.shared.config.OpenApiConfig;
import br.com.meunegocio360.core.shared.error.Problema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticação (RF-BE-001). Controller fino (RNF-BE-01): recebe DTO, delega ao caso de
 * uso e devolve DTO; o refresh token trafega apenas no cookie.
 */
@Tag(name = "Autenticação", description = "Login, renovação de sessão, logout e usuário atual")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final String PROBLEM_JSON = "application/problem+json";

  private final AutenticarUsuario autenticarUsuario;
  private final RenovarSessao renovarSessao;
  private final EncerrarSessao encerrarSessao;
  private final ConsultarUsuarioAutenticado consultarUsuarioAutenticado;
  private final CookieRefreshToken cookie;

  public AuthController(
      AutenticarUsuario autenticarUsuario,
      RenovarSessao renovarSessao,
      EncerrarSessao encerrarSessao,
      ConsultarUsuarioAutenticado consultarUsuarioAutenticado,
      CookieRefreshToken cookie) {
    this.autenticarUsuario = autenticarUsuario;
    this.renovarSessao = renovarSessao;
    this.encerrarSessao = encerrarSessao;
    this.consultarUsuarioAutenticado = consultarUsuarioAutenticado;
    this.cookie = cookie;
  }

  @Operation(
      summary = "Autentica por e-mail e senha",
      description = "Devolve o token de acesso no corpo e o refresh token no cookie mn360_refresh.")
  @ApiResponse(responseCode = "200", description = "Autenticado; cookie de refresh em Set-Cookie")
  @ApiResponse(
      responseCode = "400",
      description = "VALIDATION_ERROR",
      content =
          @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = Problema.class)))
  @ApiResponse(
      responseCode = "401",
      description = "INVALID_CREDENTIALS",
      content =
          @Content(
              mediaType = PROBLEM_JSON,
              schema = @Schema(implementation = Problema.class),
              examples =
                  @ExampleObject(
                      value =
                          """
                          {"type":"https://meunegocio360.com.br/problemas/credenciais-invalidas",
                           "title":"Credenciais inválidas","status":401,
                           "detail":"E-mail ou senha inválidos.","instance":"/api/v1/auth/login",
                           "code":"INVALID_CREDENTIALS","traceId":"9f1c2e7a-..."}
                          """)))
  @ApiResponse(
      responseCode = "429",
      description = "ACCOUNT_TEMPORARILY_LOCKED (cabeçalho Retry-After em segundos)",
      content =
          @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = Problema.class)))
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    return comSessao(autenticarUsuario.executar(request.email(), request.senha()));
  }

  @Operation(
      summary = "Renova a sessão",
      description =
          "Usa o cookie mn360_refresh (rotativo): revoga o token apresentado e emite um novo.")
  @ApiResponse(responseCode = "200", description = "Sessão renovada; novo cookie em Set-Cookie")
  @ApiResponse(
      responseCode = "401",
      description = "INVALID_REFRESH_TOKEN (o cookie é limpo)",
      content =
          @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = Problema.class)))
  @ApiResponse(
      responseCode = "403",
      description = "ORIGIN_NOT_ALLOWED",
      content =
          @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = Problema.class)))
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
      @CookieValue(name = CookieRefreshToken.NOME, required = false) String refreshToken) {
    try {
      return comSessao(renovarSessao.executar(refreshToken));
    } catch (RefreshTokenInvalidoException e) {
      throw e.comCabecalho(HttpHeaders.SET_COOKIE, cookie.expirar());
    }
  }

  @Operation(
      summary = "Encerra a sessão",
      description = "Revoga a família do refresh token e expira o cookie. Idempotente.")
  @ApiResponse(responseCode = "204", description = "Sessão encerrada (sempre)")
  @ApiResponse(
      responseCode = "403",
      description = "ORIGIN_NOT_ALLOWED",
      content =
          @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = Problema.class)))
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = CookieRefreshToken.NOME, required = false) String refreshToken) {
    encerrarSessao.executar(refreshToken);
    return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.expirar()).build();
  }

  @Operation(summary = "Dados do usuário autenticado")
  @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
  @ApiResponse(responseCode = "200", description = "Usuário autenticado")
  @ApiResponse(
      responseCode = "401",
      description = "UNAUTHORIZED (cabeçalho WWW-Authenticate: Bearer)",
      content =
          @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = Problema.class)))
  @GetMapping("/me")
  public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
    return UsuarioResponse.de(
        consultarUsuarioAutenticado.executar(UUID.fromString(jwt.getSubject())));
  }

  private ResponseEntity<TokenResponse> comSessao(SessaoEmitida sessao) {
    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            cookie.criar(sessao.refreshToken(), sessao.validadeRefreshToken()))
        .body(TokenResponse.de(sessao));
  }
}
