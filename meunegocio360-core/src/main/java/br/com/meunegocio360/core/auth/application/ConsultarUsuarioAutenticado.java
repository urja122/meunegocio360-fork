package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Dados do usuário dono do token de acesso ({@code GET /auth/me}). */
@Service
public class ConsultarUsuarioAutenticado {

  private final UsuarioRepository usuarios;

  public ConsultarUsuarioAutenticado(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  @Transactional(readOnly = true)
  public UsuarioAutenticado executar(UUID usuarioId) {
    return usuarios
        .buscarPorId(usuarioId)
        .filter(Usuario::isAtivo)
        .map(UsuarioAutenticado::de)
        .orElseThrow(UsuarioNaoAutenticadoException::new);
  }
}
