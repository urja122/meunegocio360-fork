package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Implementação JPA (Spring Data) da porta {@link RefreshTokenRepository}. */
interface JpaRefreshTokenRepository
    extends JpaRepository<RefreshToken, UUID>, RefreshTokenRepository {

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from RefreshToken t join fetch t.usuario where t.tokenHash = :tokenHash")
  Optional<RefreshToken> buscarPorHashParaAtualizacao(@Param("tokenHash") String tokenHash);

  @Override
  @Modifying(flushAutomatically = true)
  @Query(
      """
      update RefreshToken t
         set t.revogadoEm = :agora, t.atualizadoEm = :agora, t.versao = t.versao + 1
       where t.familiaId = :familiaId and t.revogadoEm is null
      """)
  int revogarFamilia(@Param("familiaId") UUID familiaId, @Param("agora") Instant agora);

  @Override
  @Modifying(flushAutomatically = true)
  @Query(
      """
      update RefreshToken t
         set t.revogadoEm = :agora, t.atualizadoEm = :agora, t.versao = t.versao + 1
       where t.usuario.id = :usuarioId and t.revogadoEm is null
      """)
  int revogarTodosDoUsuario(@Param("usuarioId") UUID usuarioId, @Param("agora") Instant agora);

  @Override
  @Modifying
  @Query(
      """
      delete from RefreshToken t
       where t.expiraEm < :limite or t.revogadoEm < :limite
      """)
  int removerExpiradosOuRevogadosAntesDe(@Param("limite") Instant limite);

  @Override
  default RefreshToken salvar(RefreshToken refreshToken) {
    return save(refreshToken);
  }
}
