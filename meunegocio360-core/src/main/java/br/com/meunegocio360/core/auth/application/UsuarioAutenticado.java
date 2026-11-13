package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.Usuario;
import java.util.UUID;

/** Dados públicos do usuário autenticado (sem hash nem campos internos, RNF-BE-02). */
public record UsuarioAutenticado(UUID id, String nome, String email, String perfil) {

  static UsuarioAutenticado de(Usuario usuario) {
    return new UsuarioAutenticado(
        usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());
  }
}
