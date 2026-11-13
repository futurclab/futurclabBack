package org.vedruna.futurjob.controller.handler;

import io.jsonwebtoken.JwtException;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Controlador de Asesoramiento REST (Security Exception Handler) para gestionar de forma
 * centralizada las excepciones de Spring Security y seguridad JWT lanzadas por la aplicación,
 * transformándolas en respuestas HTTP estandarizadas basadas en la especificación RFC 7807
 * (ProblemDetail).
 *
 * <p>Captura excepciones de autenticación (credenciales inválidas, usuario no encontrado, cuenta
 * bloqueada, expirada o deshabilitada), excepciones de autorización (acceso denegado por roles o
 * permisos insuficientes mediante anotaciones como {@code @PreAuthorize}) y fallos en la validación
 * de tokens JWT.
 *
 * <p>{@code @AllArgsConstructor}: Genera el constructor con todos los argumentos (Lombok).
 * {@code @Slf4j}: Habilita el registro de trazas con SLF4J (Lombok). {@code @RestControllerAdvice}:
 * Intercepta excepciones producidas en los controladores REST y serializa la respuesta directamente
 * en el cuerpo HTTP. {@code @Order(Ordered.HIGHEST_PRECEDENCE)}: Asegura que los manejadores de
 * seguridad tengan prioridad máxima de resolución frente a manejadores genéricos de excepciones.
 */
@AllArgsConstructor
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HandlerSecurityExController {

  /**
   * Maneja la excepción {@code AccessDeniedException}, lanzada cuando un usuario autenticado
   * intenta acceder a un recurso protegido sin disponer de los roles o privilegios necesarios (por
   * ejemplo, en endpoints anotados con {@code @PreAuthorize("hasRole('ROLE_ADMIN')")} en {@code
   * MainController}). Genera una respuesta 403 Forbidden.
   *
   * @param ex La excepción {@code AccessDeniedException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 403 Forbidden y detalles del problema.
   */
  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity<Object> handleAccessDenied(
      AccessDeniedException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "You do not have the required permissions or roles to access this resource.");
    // Establecemos el título del error.
    problemDetail.setTitle("AccessDeniedException: Access Denied");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/AccessDeniedException.html"));
    // Añadimos una propiedad para mayor claridad semántica.
    problemDetail.setProperty("access", "denied");
    // Registramos el error en el log.
    log.warn("Access Denied Exception (403 Forbidden): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado FORBIDDEN.
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code BadCredentialsException}, lanzada durante el inicio de sesión en
   * {@code AuthServiceImpl.login} cuando las credenciales proporcionadas (contraseña o nombre de
   * usuario) no coinciden con los registros almacenados. Genera una respuesta 401 Unauthorized.
   *
   * @param ex La excepción {@code BadCredentialsException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles del problema.
   */
  @ExceptionHandler(BadCredentialsException.class)
  protected ResponseEntity<Object> handleBadCredentials(
      BadCredentialsException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "The credentials provided are invalid. Please check your username and password.");
    // Establecemos el título del error.
    problemDetail.setTitle("BadCredentialsException: Invalid Credentials");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/BadCredentialsException.html"));
    // Añadimos una propiedad para mayor claridad semántica.
    problemDetail.setProperty("error", "Invalid credentials");
    // Registramos el error en el log.
    log.warn("Bad Credentials Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code UsernameNotFoundException}, lanzada cuando se intenta cargar los
   * detalles de un usuario a través de {@code UserDetailsService} pero el nombre de usuario no
   * existe en la base de datos. Genera una respuesta 401 Unauthorized.
   *
   * @param ex La excepción {@code UsernameNotFoundException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles del problema.
   */
  @ExceptionHandler(UsernameNotFoundException.class)
  protected ResponseEntity<Object> handleUsernameNotFound(
      UsernameNotFoundException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                ? ex.getMessage()
                : "The requested user could not be found.");
    // Establecemos el título del error.
    problemDetail.setTitle("UsernameNotFoundException: User Not Found");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UsernameNotFoundException.html"));
    // Añadimos una propiedad para mayor claridad semántica.
    problemDetail.setProperty("error", "User not found");
    // Registramos el error en el log.
    log.warn("Username Not Found Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code LockedException}, lanzada cuando un usuario intenta autenticarse
   * pero su cuenta se encuentra bloqueada (determinado por el campo {@code isLocked} de {@code
   * UserEntity} y el método {@code isAccountNonLocked()}). Genera una respuesta 401 Unauthorized.
   *
   * @param ex La excepción {@code LockedException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles del bloqueo.
   */
  @ExceptionHandler(LockedException.class)
  protected ResponseEntity<Object> handleLocked(LockedException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "The user account is locked. Please contact support or an administrator to unlock your account.");
    // Establecemos el título del error.
    problemDetail.setTitle("LockedException: Account Locked");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/LockedException.html"));
    // Añadimos el estado de la cuenta como propiedad del problema.
    problemDetail.setProperty("accountStatus", "locked");
    // Registramos el error en el log.
    log.warn("Account Locked Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code DisabledException}, lanzada cuando un usuario intenta autenticarse
   * pero su cuenta se encuentra inactiva o deshabilitada (determinado por el campo {@code
   * isEnabled} de {@code UserEntity} y el método {@code isEnabled()}). Genera una respuesta 401
   * Unauthorized.
   *
   * @param ex La excepción {@code DisabledException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles de la cuenta inactiva.
   */
  @ExceptionHandler(DisabledException.class)
  protected ResponseEntity<Object> handleDisabled(DisabledException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "The user account is disabled. Please verify or activate your account before attempting to log in.");
    // Establecemos el título del error.
    problemDetail.setTitle("DisabledException: Account Disabled");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/DisabledException.html"));
    // Añadimos el estado de la cuenta como propiedad del problema.
    problemDetail.setProperty("accountStatus", "disabled");
    // Registramos el error en el log.
    log.warn("Account Disabled Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code AccountExpiredException}, lanzada cuando el periodo de validez de la
   * cuenta del usuario ha expirado (determinado por el campo {@code accountExpirationDate} de
   * {@code UserEntity} y el método {@code isAccountNonExpired()}). Genera una respuesta 401
   * Unauthorized.
   *
   * @param ex La excepción {@code AccountExpiredException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles de la expiración de la
   *     cuenta.
   */
  @ExceptionHandler(AccountExpiredException.class)
  protected ResponseEntity<Object> handleAccountExpired(
      AccountExpiredException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "The user account has expired. Please contact an administrator to renew your account.");
    // Establecemos el título del error.
    problemDetail.setTitle("AccountExpiredException: Account Expired");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/AccountExpiredException.html"));
    // Añadimos el estado de la cuenta como propiedad del problema.
    problemDetail.setProperty("accountStatus", "expired");
    // Registramos el error en el log.
    log.warn("Account Expired Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code CredentialsExpiredException}, lanzada cuando las credenciales
   * (contraseña) del usuario han expirado y deben ser renovadas (determinado por el campo {@code
   * credentialsExpirationDate} de {@code UserEntity} y el método {@code
   * isCredentialsNonExpired()}). Genera una respuesta 401 Unauthorized.
   *
   * @param ex La excepción {@code CredentialsExpiredException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles de la expiración de
   *     credenciales.
   */
  @ExceptionHandler(CredentialsExpiredException.class)
  protected ResponseEntity<Object> handleCredentialsExpired(
      CredentialsExpiredException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "The user credentials (password) have expired. Please reset or update your password to proceed.");
    // Establecemos el título del error.
    problemDetail.setTitle("CredentialsExpiredException: Credentials Expired");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/CredentialsExpiredException.html"));
    // Añadimos el estado de las credenciales como propiedad del problema.
    problemDetail.setProperty("credentialsStatus", "expired");
    // Registramos el error en el log.
    log.warn("Credentials Expired Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja excepciones genéricas derivadas de {@code AccountStatusException} que no hayan sido
   * capturadas por los manejadores específicos anteriores de estado de cuenta. Genera una respuesta
   * 401 Unauthorized.
   *
   * @param ex La excepción {@code AccountStatusException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles del error de estado.
   */
  @ExceptionHandler(AccountStatusException.class)
  protected ResponseEntity<Object> handleAccountStatus(
      AccountStatusException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                ? ex.getMessage()
                : "Authentication failed due to the current status of the user account.");
    // Establecemos el título del error.
    problemDetail.setTitle("AccountStatusException: Account Status Error");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/AccountStatusException.html"));
    // Registramos el error en el log.
    log.warn("Account Status Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code InsufficientAuthenticationException}, lanzada cuando un cliente
   * anónimo o con autenticación insuficiente intenta acceder a un endpoint protegido que requiere
   * autenticación completa con token JWT. Genera una respuesta 401 Unauthorized.
   *
   * @param ex La excepción {@code InsufficientAuthenticationException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles del problema.
   */
  @ExceptionHandler(InsufficientAuthenticationException.class)
  protected ResponseEntity<Object> handleInsufficientAuthentication(
      InsufficientAuthenticationException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Full authentication is required to access this resource. Please provide a valid Bearer token.");
    // Establecemos el título del error.
    problemDetail.setTitle("InsufficientAuthenticationException: Insufficient Authentication");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/InsufficientAuthenticationException.html"));
    // Añadimos una propiedad para mayor claridad semántica.
    problemDetail.setProperty("auth", "insufficient");
    // Registramos el error en el log.
    log.warn("Insufficient Authentication Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja la excepción {@code AuthenticationCredentialsNotFoundException}, lanzada cuando un
   * componente de seguridad intercepta una llamada a un método seguro pero no encuentra ningún
   * objeto {@code Authentication} en el {@code SecurityContext}. Genera una respuesta 401
   * Unauthorized.
   *
   * @param ex La excepción {@code AuthenticationCredentialsNotFoundException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles del problema.
   */
  @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
  protected ResponseEntity<Object> handleAuthenticationCredentialsNotFound(
      AuthenticationCredentialsNotFoundException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "No authentication credentials were found in the security context to process this request.");
    // Establecemos el título del error.
    problemDetail.setTitle("AuthenticationCredentialsNotFoundException: Credentials Not Found");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/AuthenticationCredentialsNotFoundException.html"));
    // Añadimos una propiedad para mayor claridad semántica.
    problemDetail.setProperty("auth", "credentials_not_found");
    // Registramos el error en el log.
    log.warn(
        "Authentication Credentials Not Found Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja excepciones de tipo {@code JwtException} producidas durante el parseo, verificación de
   * firma o expiración de JSON Web Tokens (Access Tokens o Refresh Tokens) a través de la librería
   * JJWT empleada en {@code JwtServiceImpl}. Genera una respuesta 401 Unauthorized.
   *
   * @param ex La excepción {@code JwtException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles del error del token.
   */
  @ExceptionHandler(JwtException.class)
  protected ResponseEntity<Object> handleJwtException(JwtException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "The provided JSON Web Token is invalid, malformed, or has expired. Please authenticate again.");
    // Establecemos el título del error.
    problemDetail.setTitle("JwtException: Invalid or Expired Token");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://javadoc.io/doc/io.jsonwebtoken/jjwt-api/latest/io/jsonwebtoken/JwtException.html"));
    // Añadimos una propiedad indicando el error del token.
    problemDetail.setProperty("token", "invalid_or_expired");
    // Registramos el error en el log.
    log.warn("JWT Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Maneja de manera genérica cualquier otra excepción derivada de {@code AuthenticationException}
   * que no haya sido capturada por los manejadores más específicos de autenticación. Genera una
   * respuesta 401 Unauthorized.
   *
   * @param ex La excepción {@code AuthenticationException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con estado 401 Unauthorized y detalles de la falla de
   *     autenticación.
   */
  @ExceptionHandler(AuthenticationException.class)
  protected ResponseEntity<Object> handleAuthentication(
      AuthenticationException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la excepcion
    // ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                ? ex.getMessage()
                : "An authentication error occurred while processing the request.");
    // Establecemos el título del error.
    problemDetail.setTitle("AuthenticationException: Authentication Failed");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/AuthenticationException.html"));
    // Añadimos una propiedad para mayor claridad semántica.
    problemDetail.setProperty("error", "Authentication failed");
    // Registramos el error en el log.
    log.warn("General Authentication Exception (401 Unauthorized): {}", ex.getMessage());
    // Devolvemos la respuesta con el ProblemDetail y el estado UNAUTHORIZED.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }
}
