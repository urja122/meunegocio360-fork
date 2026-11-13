package br.com.meunegocio360.core.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio único da aplicação, em UTC. Casos de uso recebem o {@link Clock} por injeção em vez de
 * chamar {@code Instant.now()}, o que torna as regras de tempo testáveis.
 */
@Configuration(proxyBeanMethods = false)
public class RelogioConfig {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
