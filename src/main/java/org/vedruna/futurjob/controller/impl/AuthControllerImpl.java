package org.vedruna.futurjob.controller.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.vedruna.futurjob.controller.AuthController;
import org.vedruna.futurjob.controller.converter.UserConverter;
import org.vedruna.futurjob.controller.dto.UserDto;
import org.vedruna.futurjob.controller.dto.authdto.AuthResponseDto;
import org.vedruna.futurjob.controller.dto.authdto.LoginRequestDto;
import org.vedruna.futurjob.controller.dto.authdto.RefreshRequestDto;
import org.vedruna.futurjob.controller.dto.authdto.RegisterRequestDto;
import org.vedruna.futurjob.persistance.model.UserEntity;
import org.vedruna.futurjob.service.AuthService;

/**
 * Controlador REST encargado de manejar todas las peticiones relacionadas con la autenticación y
 * gestión de usuarios (registro, login, obtener usuario actual).
 *
 * <p>Utiliza el path base "/auth" y permite peticiones de cualquier origen (debido a @CrossOrigin).
 */
@CrossOrigin
@RestController
@AllArgsConstructor
public class AuthControllerImpl implements AuthController {
  /** Servicio de lógica de negocio para las operaciones de autenticación. */
  private final AuthService authService;

  /** Componente para convertir entre entidades User y DTOs. */
  private final UserConverter userConverter;

  /**
   * Endpoint para registrar un nuevo usuario en el sistema.
   *
   * <p>Recibe un DTO de solicitud de registro, lo convierte a entidad, lo registra a través del
   * servicio y devuelve el DTO del usuario creado con un estado HTTP 201 (CREATED).
   *
   * @param request El DTO que contiene los datos de registro (nombre, contraseña, email, etc.).
   * @return ResponseEntity que contiene el UserDTO del usuario registrado y el estado HTTP CREATED.
   */
  public ResponseEntity<UserDto> register(RegisterRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userConverter.toDto(authService.register(userConverter.registerToEntity(request))));
  }

  /**
   * Endpoint para la autenticación (inicio de sesión) de un usuario.
   *
   * <p>Recibe un DTO de solicitud de login, lo convierte a entidad, llama al servicio para
   * autenticar y obtener el token JWT, y devuelve el token con un estado HTTP 200 (OK).
   *
   * @param request El DTO que contiene las credenciales de login (nombre de usuario y contraseña).
   * @return ResponseEntity que contiene el AuthResponseDTO (con el JWT) y el estado HTTP OK.
   */
  public ResponseEntity<AuthResponseDto> login(LoginRequestDto request) {
    return ResponseEntity.ok(authService.login(userConverter.loginToEntity(request)));
  }

  /**
   * Endpoint para obtener los datos del usuario actualmente autenticado a través del token JWT.
   *
   * <p>Utiliza la anotación @AuthenticationPrincipal de Spring Security para inyectar
   * automáticamente la entidad User del usuario logueado.
   *
   * @param userLogueado La entidad User del usuario autenticado, inyectada por Spring Security.
   * @return UserDTO que contiene la información pública del usuario.
   */
  public UserDto me(UserEntity userLogueado) {
    return userConverter.toDto(userLogueado);
  }

  /**
   * Endpoint para renovar el Access Token utilizando un Refresh Token.
   *
   * @param request El cuerpo de la solicitud que contiene el Refresh Token.
   * @return ResponseEntity con el nuevo Access Token y el Refresh Token.
   */
  public ResponseEntity<AuthResponseDto> refreshToken(RefreshRequestDto request) {
    // Devuelve 200 OK con el nuevo par de tokens
    return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
  }
}
