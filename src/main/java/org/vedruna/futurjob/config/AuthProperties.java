package org.vedruna.futurjob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

  /**
   * Clave secreta (Base64) utilizada para firmar y verificar el Access Token. Inyectada desde la
   * configuración de la aplicación (e.g., application.properties).
   */
  private String accessTokenSecretKey;

  /**
   * Tiempo de vida del Access Token en milisegundos. Debe ser un periodo corto (máximo unos 3
   * minutos!).
   */
  private Long accessTokenExpiration;

  /**
   * Clave secreta (Base64) utilizada para firmar y verificar el Refresh Token. Se recomienda que
   * sea distinta a la del Access Token por seguridad.
   */
  private String refreshTokenSecretKey;

  /**
   * Tiempo de vida del Refresh Token en milisegundos. Debe ser un periodo largo (máximo unos 7
   * días!).
   */
  private Long refreshTokenExpiration;
}
