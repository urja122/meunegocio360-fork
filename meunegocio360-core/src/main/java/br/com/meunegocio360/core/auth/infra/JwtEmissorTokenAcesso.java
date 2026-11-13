package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.application.AuthProperties;
import br.com.meunegocio360.core.auth.domain.EmissorTokenAcesso;
import br.com.meunegocio360.core.auth.domain.Usuario;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Emite o JWT de acesso com as claims {@code iss}, {@code sub}, {@code iat}, {@code exp}, {@code
 * jti} e {@code perfil}. Não inclui senha, hash, e-mail nem outros dados pessoais.
 */
@Component
class JwtEmissorTokenAcesso implements EmissorTokenAcesso {

  static final String CLAIM_PERFIL = "perfil";

  private final JwtEncoder encoder;
  private final AuthProperties properties;

  JwtEmissorTokenAcesso(JwtEncoder encoder, AuthProperties properties) {
    this.encoder = encoder;
    this.properties = properties;
  }

  @Override
  public String emitir(Usuario usuario, Instant agora) {
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(properties.jwt().issuer())
            .subject(usuario.getId().toString())
            .issuedAt(agora)
            .expiresAt(agora.plus(properties.jwt().validade()))
            .id(UUID.randomUUID().toString())
            .claim(CLAIM_PERFIL, usuario.getPerfil().name())
            .build();
    JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
    return encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
  }
}
