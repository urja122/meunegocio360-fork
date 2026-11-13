package br.com.meunegocio360.core.auth.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistência de refresh tokens; implementada em {@code auth.infra}. */
public interface RefreshTokenRepository {

  /**
   * Busca com bloqueio pessimista: duas renovações simultâneas com o mesmo token são serializadas e
   * apenas a primeira vence.
   */
  Optional<RefreshToken> buscarPorHashParaAtualizacao(String tokenHash);

  RefreshToken salvar(RefreshToken refreshToken);

  /** RN-08 e RN-10: revoga todos os tokens ainda ativos da família. */
  int revogarFamilia(UUID familiaId, Instant agora);

  /** RN-13: revoga todas as famílias ativas do usuário. */
  int revogarTodosDoUsuario(UUID usuarioId, Instant agora);

  /** Remove tokens expirados ou revogados antes do limite informado. */
  int removerExpiradosOuRevogadosAntesDe(Instant limite);
}
