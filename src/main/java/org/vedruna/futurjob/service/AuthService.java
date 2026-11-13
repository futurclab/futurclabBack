package org.vedruna.futurjob.service;

import org.vedruna.futurjob.controller.dto.authdto.AuthResponseDto;
import org.vedruna.futurjob.persistance.model.UserEntity;

/** Interfaz que define las operaciones del servicio de autenticación y gestión de usuarios. */
public interface AuthService {

  /**
   * Autentica un usuario con sus credenciales y genera los tokens de acceso requeridos.
   *
   * @param user Entidad usuario con las credenciales de inicio de sesión.
   * @return DTO con los tokens generados e información de expiración.
   */
  AuthResponseDto login(UserEntity user);

  /**
   * Registra un nuevo usuario en el sistema con el rol predeterminado y contraseña encriptada.
   *
   * @param user Entidad usuario con los datos de registro.
   * @return La entidad guardada en la base de datos.
   */
  UserEntity register(UserEntity user);

  /**
   * Emite un nuevo Access Token a partir de un Refresh Token válido.
   *
   * @param refreshToken El Refresh Token del cliente.
   * @return DTO con el nuevo Access Token.
   */
  AuthResponseDto refreshToken(String refreshToken);
}
