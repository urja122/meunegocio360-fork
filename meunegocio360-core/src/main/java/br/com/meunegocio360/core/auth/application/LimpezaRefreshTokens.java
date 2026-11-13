package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Remove refresh tokens expirados ou revogados há mais de 30 dias (modelo de dados, RNF-025). */
@Component
public class LimpezaRefreshTokens {

  private static final Logger log = LoggerFactory.getLogger(LimpezaRefreshTokens.class);

  private final RefreshTokenRepository refreshTokens;
  private final AuthProperties properties;
  private final Clock clock;

  public LimpezaRefreshTokens(
      RefreshTokenRepository refreshTokens, AuthProperties properties, Clock clock) {
    this.refreshTokens = refreshTokens;
    this.properties = properties;
    this.clock = clock;
  }

  @Scheduled(cron = "${app.auth.refresh.limpeza-cron}", zone = "UTC")
  @Transactional
  public void executar() {
    Instant limite = clock.instant().minus(properties.refresh().retencao());
    int removidos = refreshTokens.removerExpiradosOuRevogadosAntesDe(limite);
    log.info("auth.refresh.limpeza removidos={}", removidos);
  }
}
