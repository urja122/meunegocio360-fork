package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logout idempotente: revoga a família do refresh token apresentado, se existir (RN-10). */
@Service
public class EncerrarSessao {

  private static final Logger log = LoggerFactory.getLogger(EncerrarSessao.class);

  private final RefreshTokenRepository refreshTokens;
  private final GeradorRefreshToken geradorRefreshToken;
  private final Clock clock;

  public EncerrarSessao(
      RefreshTokenRepository refreshTokens, GeradorRefreshToken geradorRefreshToken, Clock clock) {
    this.refreshTokens = refreshTokens;
    this.geradorRefreshToken = geradorRefreshToken;
    this.clock = clock;
  }

  @Transactional
  public void executar(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }
    refreshTokens
        .buscarPorHashParaAtualizacao(geradorRefreshToken.hash(refreshToken))
        .ifPresent(token -> {
          refreshTokens.revogarFamilia(token.getFamiliaId(), clock.instant());
          log.info("auth.logout usuarioId={}", token.getUsuario().getId());
        });
  }
}
