package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.shared.error.ApiException;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Renovação de sessão com refresh token rotativo e detecção de reuso (RN-02, RN-07, RN-08, RN-13).
 */
@Service
public class RenovarSessao {

  private static final Logger log = LoggerFactory.getLogger(RenovarSessao.class);

  private final RefreshTokenRepository refreshTokens;
  private final GeradorRefreshToken geradorRefreshToken;
  private final EmissaoSessao emissaoSessao;
  private final Clock clock;

  public RenovarSessao(
      RefreshTokenRepository refreshTokens,
      GeradorRefreshToken geradorRefreshToken,
      EmissaoSessao emissaoSessao,
      Clock clock) {
    this.refreshTokens = refreshTokens;
    this.geradorRefreshToken = geradorRefreshToken;
    this.emissaoSessao = emissaoSessao;
    this.clock = clock;
  }

  /**
   * O token é lido com bloqueio pessimista: renovações simultâneas com o mesmo valor são
   * serializadas; a segunda encontra o token já revogado e é tratada como reuso. A revogação da
   * família é gravada mesmo com a resposta 401 ({@code noRollbackFor}).
   */
  @Transactional(noRollbackFor = ApiException.class)
  public SessaoEmitida executar(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new RefreshTokenInvalidoException();
    }
    Instant agora = clock.instant();
    RefreshToken atual =
        refreshTokens
            .buscarPorHashParaAtualizacao(geradorRefreshToken.hash(refreshToken))
            .orElseThrow(RefreshTokenInvalidoException::new);

    Usuario usuario = atual.getUsuario();
    if (atual.estaRevogado()) {
      refreshTokens.revogarFamilia(atual.getFamiliaId(), agora);
      log.warn(
          "auth.refresh.reuso_detectado usuarioId={} familiaId={}",
          usuario.getId(),
          atual.getFamiliaId());
      throw new RefreshTokenInvalidoException();
    }
    if (atual.estaExpirado(agora)) {
      throw new RefreshTokenInvalidoException();
    }
    if (!usuario.podeAutenticar(agora)) {
      refreshTokens.revogarFamilia(atual.getFamiliaId(), agora);
      log.info("auth.refresh.recusado motivo=usuario_indisponivel usuarioId={}", usuario.getId());
      throw new RefreshTokenInvalidoException();
    }

    atual.revogar(agora);
    return emissaoSessao.emitir(usuario, atual.getFamiliaId(), agora);
  }
}
