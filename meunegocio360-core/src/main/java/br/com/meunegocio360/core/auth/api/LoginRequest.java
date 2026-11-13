package br.com.meunegocio360.core.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciais de login (RN-01, RN-11). Validadas no servidor (RNF-020). */
public record LoginRequest(
    @Schema(example = "admin@meunegocio360.com.br")
        @NotBlank(message = "é obrigatório")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String email,
    @Schema(example = "********", format = "password")
        @NotBlank(message = "é obrigatória")
        @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
        String senha) {

  /** Remove espaços nas pontas do e-mail antes da validação (RN-01). */
  public LoginRequest {
    email = email == null ? null : email.trim();
  }

  /** Nunca exibe a senha em logs. */
  @Override
  public String toString() {
    return "LoginRequest[email=***, senha=***]";
  }
}
