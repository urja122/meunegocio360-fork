package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.application.AuthProperties;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Emissão e validação do JWT de acesso com HS256 e segredo de {@code JWT_SECRET} (RF-BE-001). A
 * migração para RS256 + JWKS está prevista em RNF-094.
 */
@Configuration(proxyBeanMethods = false)
class JwtConfig {

  private final SecretKey chave;
  private final String issuer;

  JwtConfig(AuthProperties properties) {
    this.chave =
        new SecretKeySpec(
            properties.jwt().secret().getBytes(StandardCharsets.UTF_8),
            MacAlgorithm.HS256.getName());
    this.issuer = properties.jwt().issuer();
  }

  @Bean
  JwtEncoder jwtEncoder() {
    return NimbusJwtEncoder.withSecretKey(chave).algorithm(MacAlgorithm.HS256).build();
  }

  /**
   * Aceita somente HS256 (tokens com {@code alg: none} ou outro algoritmo são rejeitados) e valida
   * assinatura, {@code exp}/{@code nbf} e {@code iss}.
   */
  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
    return decoder;
  }
}
