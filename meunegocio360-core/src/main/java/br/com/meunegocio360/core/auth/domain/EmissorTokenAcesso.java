package br.com.meunegocio360.core.auth.domain;

import java.time.Instant;

/** Porta de emissão do token de acesso (JWT); implementada em {@code auth.infra}. */
public interface EmissorTokenAcesso {

  /** Emite um token de acesso para o usuário, válido a partir de {@code agora}. */
  String emitir(Usuario usuario, Instant agora);
}
