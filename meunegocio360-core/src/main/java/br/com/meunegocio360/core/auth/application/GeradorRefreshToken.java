package br.com.meunegocio360.core.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * RN-06 e RN-09: gera o valor opaco do refresh token (256 bits de {@link SecureRandom}, Base64
 * URL-safe) e calcula o hash SHA-256 (hex) que é gravado no banco.
 */
@Component
public class GeradorRefreshToken {

  private static final int TAMANHO_BYTES = 32;

  private final SecureRandom random = new SecureRandom();

  public String gerar() {
    byte[] bytes = new byte[TAMANHO_BYTES];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 indisponível na JVM", e);
    }
  }
}
