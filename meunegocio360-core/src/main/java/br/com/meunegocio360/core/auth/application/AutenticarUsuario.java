package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepository;
import br.com.meunegocio360.core.shared.error.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso de login por e-mail e senha (RF-BE-001, RN-01 a RN-05, RN-12, RN-13). */
@Service
public class AutenticarUsuario {

  private static final Logger log = LoggerFactory.getLogger(AutenticarUsuario.class);

  /** Limite do BCrypt (RN-11); senhas maiores nunca conferem. */
  private static final int LIMITE_BYTES_BCRYPT = 72;

  private final UsuarioRepository usuarios;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder passwordEncoder;
  private final EmissaoSessao emissaoSessao;
  private final AuthProperties properties;
  private final Clock clock;

  /** Hash fictício para igualar o tempo de resposta de e-mails inexistentes (RN-03). */
  private final String hashFicticio;

  public AutenticarUsuario(
      UsuarioRepository usuarios,
      RefreshTokenRepository refreshTokens,
      PasswordEncoder passwordEncoder,
      EmissaoSessao emissaoSessao,
      AuthProperties properties,
      Clock clock) {
    this.usuarios = usuarios;
    this.refreshTokens = refreshTokens;
    this.passwordEncoder = passwordEncoder;
    this.emissaoSessao = emissaoSessao;
    this.properties = properties;
    this.clock = clock;
    this.hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
  }

  /**
   * As exceções de negócio NÃO desfazem a transação: o incremento do contador de falhas, o bloqueio
   * e a revogação de sessões precisam ser gravados mesmo com a resposta de erro.
   */
  @Transactional(noRollbackFor = ApiException.class)
  public SessaoEmitida executar(String email, String senha) {
    Instant agora = clock.instant();
    Optional<Usuario> encontrado =
        usuarios.buscarPorEmailParaAtualizacao(Usuario.normalizarEmail(email));

    if (encontrado.isEmpty()) {
      senhaConfere(senha, hashFicticio);
      log.info("auth.login.falha motivo=credenciais email={}", MascaraEmail.mascarar(email));
      throw new CredenciaisInvalidasException();
    }

    Usuario usuario = encontrado.get();
    if (!usuario.isAtivo()) {
      senhaConfere(senha, usuario.getSenhaHash());
      log.info("auth.login.falha motivo=inativo usuarioId={}", usuario.getId());
      throw new CredenciaisInvalidasException();
    }

    if (usuario.estaBloqueado(agora)) {
      log.warn("auth.login.recusado motivo=bloqueado usuarioId={}", usuario.getId());
      throw new ContaBloqueadaException(usuario.tempoRestanteDeBloqueio(agora));
    }

    if (!senhaConfere(senha, usuario.getSenhaHash())) {
      registrarFalha(usuario, agora);
      throw new CredenciaisInvalidasException();
    }

    usuario.registrarLoginComSucesso(agora);
    log.info("auth.login.sucesso usuarioId={}", usuario.getId());
    return emissaoSessao.emitir(usuario, UUID.randomUUID(), agora);
  }

  private void registrarFalha(Usuario usuario, Instant agora) {
    boolean bloqueou =
        usuario.registrarFalha(properties.maxTentativas(), properties.duracaoBloqueio(), agora);
    if (bloqueou) {
      refreshTokens.revogarTodosDoUsuario(usuario.getId(), agora);
      log.warn(
          "auth.conta.bloqueada usuarioId={} minutos={}",
          usuario.getId(),
          properties.bloqueioMinutos());
    } else {
      log.info(
          "auth.login.falha motivo=senha usuarioId={} tentativas={}",
          usuario.getId(),
          usuario.getTentativasFalhas());
    }
  }

  /** Comparação sempre via {@link PasswordEncoder}, nunca manual (RNF-023). */
  private boolean senhaConfere(String senha, String hash) {
    boolean dentroDoLimite = senha.getBytes(StandardCharsets.UTF_8).length <= LIMITE_BYTES_BCRYPT;
    boolean confere = passwordEncoder.matches(dentroDoLimite ? senha : "", hash);
    return dentroDoLimite && confere;
  }
}
