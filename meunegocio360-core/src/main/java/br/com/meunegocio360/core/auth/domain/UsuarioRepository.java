package br.com.meunegocio360.core.auth.domain;

import java.util.Optional;
import java.util.UUID;

/** Porta de persistência de usuários; implementada em {@code auth.infra}. */
public interface UsuarioRepository {

  /** Busca com bloqueio pessimista, evitando condição de corrida no contador de falhas. */
  Optional<Usuario> buscarPorEmailParaAtualizacao(String email);

  Optional<Usuario> buscarPorId(UUID id);

  boolean existePorEmail(String email);

  Usuario salvar(Usuario usuario);
}
