package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.Perfil;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seed de desenvolvimento (RNF-BE-24): cria um usuário ADMIN e um USUARIO fictícios, somente no
 * perfil {@code dev}. As senhas vêm de {@code SEED_ADMIN_SENHA} e {@code SEED_USUARIO_SENHA} e
 * nunca são gravadas no repositório. É idempotente: não recria usuários existentes.
 */
@Component
@Profile("dev")
public class SeedUsuariosDev implements ApplicationRunner {

  static final String EMAIL_ADMIN = "admin@meunegocio360.com.br";
  static final String EMAIL_USUARIO = "usuario@meunegocio360.com.br";

  private static final Logger log = LoggerFactory.getLogger(SeedUsuariosDev.class);

  private final UsuarioRepository usuarios;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final String senhaAdmin;
  private final String senhaUsuario;

  public SeedUsuariosDev(
      UsuarioRepository usuarios,
      PasswordEncoder passwordEncoder,
      Clock clock,
      @Value("${app.seed.admin-senha:}") String senhaAdmin,
      @Value("${app.seed.usuario-senha:}") String senhaUsuario) {
    this.usuarios = usuarios;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.senhaAdmin = senhaAdmin;
    this.senhaUsuario = senhaUsuario;
  }

  @Override
  public void run(ApplicationArguments args) {
    criarSeAusente("Administrador", EMAIL_ADMIN, senhaAdmin, "SEED_ADMIN_SENHA", Perfil.ADMIN);
    criarSeAusente(
        "Usuário de Teste", EMAIL_USUARIO, senhaUsuario, "SEED_USUARIO_SENHA", Perfil.USUARIO);
  }

  private void criarSeAusente(
      String nome, String email, String senha, String variavel, Perfil perfil) {
    if (usuarios.existePorEmail(email)) {
      return;
    }
    validarSenha(senha, variavel);
    usuarios.salvar(
        Usuario.novo(nome, email, passwordEncoder.encode(senha), perfil, clock.instant()));
    log.info("seed.usuario.criado perfil={}", perfil);
  }

  /** Mesmos limites do login (RN-11), para que o usuário do seed consiga autenticar. */
  private static void validarSenha(String senha, String variavel) {
    int bytes = senha == null ? 0 : senha.getBytes(StandardCharsets.UTF_8).length;
    if (senha == null || senha.length() < 8 || bytes > 72) {
      throw new IllegalStateException(
          variavel + " deve ser definida no .env com 8 a 72 caracteres (perfil dev).");
    }
  }
}
