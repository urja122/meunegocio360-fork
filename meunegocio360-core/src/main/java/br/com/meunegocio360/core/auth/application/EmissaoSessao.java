package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.EmissorTokenAcesso;
import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import br.com.meunegocio360.core.auth.domain.Usuario;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Emite um par token de acesso + refresh token na família informada. Usado pelo login (nova
 * família) e pela renovação (mesma família, RN-07). Executa dentro da transação do chamador.
 */
@Component
class EmissaoSessao {

  private final EmissorTokenAcesso emissorTokenAcesso;
  private final GeradorRefreshToken geradorRefreshToken;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthProperties properties;

  EmissaoSessao(
      EmissorTokenAcesso emissorTokenAcesso,
      GeradorRefreshToken geradorRefreshToken,
      RefreshTokenRepository refreshTokenRepository,
      AuthProperties properties) {
    this.emissorTokenAcesso = emissorTokenAcesso;
    this.geradorRefreshToken = geradorRefreshToken;
    this.refreshTokenRepository = refreshTokenRepository;
    this.properties = properties;
  }

  SessaoEmitida emitir(Usuario usuario, UUID familiaId, Instant agora) {
    String refreshToken = geradorRefreshToken.gerar();
    refreshTokenRepository.salvar(
        RefreshToken.emitir(
            usuario,
            familiaId,
            geradorRefreshToken.hash(refreshToken),
            agora.plus(properties.refresh().validade()),
            agora));
    return new SessaoEmitida(
        emissorTokenAcesso.emitir(usuario, agora),
        properties.jwt().validade(),
        refreshToken,
        properties.refresh().validade(),
        UsuarioAutenticado.de(usuario));
  }
}
