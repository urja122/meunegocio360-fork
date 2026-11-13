package br.com.meunegocio360.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Meunegocio360CoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(Meunegocio360CoreApplication.class, args);
  }
}
