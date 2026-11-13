package br.com.meunegocio360.core.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita rotinas agendadas ({@code @Scheduled}), como a limpeza de refresh tokens. */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class AgendamentoConfig {}
