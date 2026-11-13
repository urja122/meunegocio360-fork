package br.com.meunegocio360.core.shared.security;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

import br.com.meunegocio360.core.shared.error.ProblemaWriter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Cadeia de segurança explícita (RNF-BE-40): sessão STATELESS e deny-by-default (RNF-021).
 *
 * <p>Exceções públicas (todas documentadas aqui):
 *
 * <ul>
 *   <li>{@code POST /api/v1/auth/login}, {@code /refresh} e {@code /logout} (RF-BE-001);
 *   <li>{@code GET /actuator/health} (RNF-051, RNF-104);
 *   <li>Swagger UI e OpenAPI, somente quando o springdoc está habilitado (desabilitado em prod,
 *       RNF-030).
 * </ul>
 *
 * <p>O CSRF do Spring fica desabilitado porque as APIs se autenticam pelo cabeçalho {@code
 * Authorization}. Os dois endpoints que usam cookie ({@code /refresh} e {@code /logout}) são
 * protegidos por {@code SameSite=Strict}, cookie restrito a {@code /api/v1/auth} e verificação do
 * {@code Origin} ({@link VerificacaoOrigemFilter}).
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

  private static final String AUTH = "/api/v1/auth";

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      OrigensPermitidasProperties origens,
      ProblemaWriter problemaWriter,
      ProblemaAuthenticationEntryPoint entryPoint,
      ProblemaAccessDeniedHandler accessDeniedHandler,
      @Value("${springdoc.api-docs.enabled:true}") boolean documentacaoHabilitada)
      throws Exception {

    var endpointsComCookie =
        new OrRequestMatcher(
            pathPattern(HttpMethod.POST, AUTH + "/refresh"),
            pathPattern(HttpMethod.POST, AUTH + "/logout"));

    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .requestCache(AbstractHttpConfigurer::disable)
        .addFilterBefore(
            new VerificacaoOrigemFilter(endpointsComCookie, origens, problemaWriter),
            CorsFilter.class)
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(HttpMethod.POST, AUTH + "/login", AUTH + "/refresh")
              .permitAll()
              .requestMatchers(HttpMethod.POST, AUTH + "/logout")
              .permitAll()
              .requestMatchers(HttpMethod.GET, "/actuator/health")
              .permitAll();
          if (documentacaoHabilitada) {
            auth.requestMatchers(
                    HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                .permitAll();
          }
          auth.anyRequest().authenticated();
        })
        .oauth2ResourceServer(
            rs -> rs.jwt(Customizer.withDefaults()).authenticationEntryPoint(entryPoint))
        .exceptionHandling(
            e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler));
    return http.build();
  }

  /** CORS por bean único, com credenciais e sem curinga (RNF-BE-42). */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(OrigensPermitidasProperties origens) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(origens.allowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-Id"));
    config.setExposedHeaders(List.of("X-Request-Id", "Retry-After", "WWW-Authenticate"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }

  /** BCrypt via DelegatingPasswordEncoder, gravando no formato {@code {bcrypt}...} (RNF-023). */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
