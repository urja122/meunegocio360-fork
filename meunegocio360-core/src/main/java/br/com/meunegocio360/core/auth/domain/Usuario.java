package br.com.meunegocio360.core.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Usuário do sistema (RF-BE-001). Nunca é exposto na API (RNF-BE-02). Não sobrescreve {@code
 * toString()} para não vazar o hash da senha em logs.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "nome", nullable = false, length = 120)
  private String nome;

  @Column(name = "email", nullable = false, length = 160, unique = true)
  private String email;

  @Column(name = "senha_hash", nullable = false, length = 100)
  private String senhaHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "perfil", nullable = false, length = 20)
  private Perfil perfil;

  @Column(name = "ativo", nullable = false)
  private boolean ativo;

  @Column(name = "tentativas_falhas", nullable = false)
  private int tentativasFalhas;

  @Column(name = "bloqueado_ate")
  private Instant bloqueadoAte;

  @Column(name = "ultimo_login_em")
  private Instant ultimoLoginEm;

  @Column(name = "criado_em", nullable = false, updatable = false)
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false)
  private Instant atualizadoEm;

  @Version
  @Column(name = "versao", nullable = false)
  private long versao;

  protected Usuario() {}

  private Usuario(String nome, String email, String senhaHash, Perfil perfil, Instant agora) {
    this.id = UUID.randomUUID();
    this.nome = Objects.requireNonNull(nome);
    this.email = normalizarEmail(email);
    this.senhaHash = Objects.requireNonNull(senhaHash);
    this.perfil = Objects.requireNonNull(perfil);
    this.ativo = true;
    this.tentativasFalhas = 0;
    this.criadoEm = agora;
    this.atualizadoEm = agora;
  }

  public static Usuario novo(
      String nome, String email, String senhaHash, Perfil perfil, Instant agora) {
    return new Usuario(nome, email, senhaHash, perfil, agora);
  }

  /** RN-01: e-mail comparado sem diferenciar maiúsculas/minúsculas, armazenado com trim. */
  public static String normalizarEmail(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }

  public boolean estaBloqueado(Instant agora) {
    return bloqueadoAte != null && bloqueadoAte.isAfter(agora);
  }

  public Duration tempoRestanteDeBloqueio(Instant agora) {
    return estaBloqueado(agora) ? Duration.between(agora, bloqueadoAte) : Duration.ZERO;
  }

  /** RN-02 e RN-13: só usuários ativos e não bloqueados autenticam ou renovam sessão. */
  public boolean podeAutenticar(Instant agora) {
    return ativo && !estaBloqueado(agora);
  }

  /**
   * RN-04: registra uma falha de senha. Ao atingir o limite, bloqueia a conta e reinicia o
   * contador, de modo que, passado o bloqueio, o usuário volte a ter todas as tentativas.
   *
   * @return {@code true} se a conta foi bloqueada nesta chamada
   */
  public boolean registrarFalha(int maxTentativas, Duration duracaoBloqueio, Instant agora) {
    tentativasFalhas++;
    atualizadoEm = agora;
    if (tentativasFalhas >= maxTentativas) {
      bloqueadoAte = agora.plus(duracaoBloqueio);
      tentativasFalhas = 0;
      return true;
    }
    return false;
  }

  /** RN-04 e RN-12: login bem-sucedido zera o contador e registra o último login. */
  public void registrarLoginComSucesso(Instant agora) {
    tentativasFalhas = 0;
    bloqueadoAte = null;
    ultimoLoginEm = agora;
    atualizadoEm = agora;
  }

  public UUID getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public String getEmail() {
    return email;
  }

  public String getSenhaHash() {
    return senhaHash;
  }

  public Perfil getPerfil() {
    return perfil;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public int getTentativasFalhas() {
    return tentativasFalhas;
  }

  public Instant getUltimoLoginEm() {
    return ultimoLoginEm;
  }
}
