package br.com.meunegocio360.core.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Refresh token rotativo (RN-06 a RN-10). Guarda apenas o hash SHA-256 do valor entregue ao cliente
 * (RN-09). Não sobrescreve {@code toString()} para não expor o hash em logs.
 */
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "usuario_id",
      columnDefinition = "BINARY(16)",
      nullable = false,
      updatable = false)
  private Usuario usuario;

  @Column(name = "familia_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
  private UUID familiaId;

  @Column(
      name = "token_hash",
      columnDefinition = "CHAR(64)",
      nullable = false,
      unique = true,
      updatable = false)
  private String tokenHash;

  @Column(name = "expira_em", nullable = false, updatable = false)
  private Instant expiraEm;

  @Column(name = "revogado_em")
  private Instant revogadoEm;

  @Column(name = "criado_em", nullable = false, updatable = false)
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false)
  private Instant atualizadoEm;

  @Version
  @Column(name = "versao", nullable = false)
  private long versao;

  protected RefreshToken() {}

  private RefreshToken(
      Usuario usuario, UUID familiaId, String tokenHash, Instant expiraEm, Instant agora) {
    this.id = UUID.randomUUID();
    this.usuario = Objects.requireNonNull(usuario);
    this.familiaId = Objects.requireNonNull(familiaId);
    this.tokenHash = Objects.requireNonNull(tokenHash);
    this.expiraEm = Objects.requireNonNull(expiraEm);
    this.criadoEm = agora;
    this.atualizadoEm = agora;
  }

  public static RefreshToken emitir(
      Usuario usuario, UUID familiaId, String tokenHash, Instant expiraEm, Instant agora) {
    return new RefreshToken(usuario, familiaId, tokenHash, expiraEm, agora);
  }

  public boolean estaRevogado() {
    return revogadoEm != null;
  }

  public boolean estaExpirado(Instant agora) {
    return !expiraEm.isAfter(agora);
  }

  public void revogar(Instant agora) {
    if (revogadoEm == null) {
      revogadoEm = agora;
      atualizadoEm = agora;
    }
  }

  public UUID getId() {
    return id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public UUID getFamiliaId() {
    return familiaId;
  }

  public Instant getExpiraEm() {
    return expiraEm;
  }
}
