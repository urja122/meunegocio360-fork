package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Dados públicos do usuário (sem campos internos ou de hash, RNF-BE-02). */
public record UsuarioResponse(
    @Schema(example = "0b6c6a8e-3a1f-4a4b-9a64-2d0f1c3f6b11") UUID id,
    @Schema(example = "Administrador") String nome,
    @Schema(example = "admin@meunegocio360.com.br") String email,
    @Schema(
            example = "ADMIN",
            allowableValues = {"ADMIN", "USUARIO"})
        String perfil) {

  static UsuarioResponse de(UsuarioAutenticado usuario) {
    return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.perfil());
  }
}
