package org.vedruna.futurjob.service.impl;

import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.vedruna.futurjob.controller.dto.authdto.AuthResponseDto;
import org.vedruna.futurjob.persistance.model.RolEntity;
import org.vedruna.futurjob.persistance.model.UserEntity;
import org.vedruna.futurjob.persistance.repository.RolRepository;
import org.vedruna.futurjob.persistance.repository.UserRepository;
import org.vedruna.futurjob.service.AuthService;

/**
 * Servicio encargado de gestionar las operaciones de autenticación (login) y registro de usuarios.
 *
 * <p>Utiliza varios componentes de Spring Security y otros servicios para realizar la validación de
 * credenciales, la gestión de roles, la codificación de contraseñas y la generación de tokens JWT.
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

  /** Repositorio para acceder y manipular los datos de la entidad User. */
  private final UserRepository userRepo;

  /** Repositorio para acceder y manipular los datos de la entidad Rol. */
  private final RolRepository rolRepo;

  /** Servicio para la creación y validación de JSON Web Tokens (JWT). */
  private final JwtServiceImpl jwtService;

  /** Componente para codificar y verificar contraseñas. */
  private final PasswordEncoder passwordEncoder;

  /** Componente central de Spring Security para gestionar la autenticación. */
  private final AuthenticationManager authenticationManager;

  /** Servicio para obtener detalles de usuario. */
  private final UserDetailsService userDetailsService;

  /**
   * Procesa la solicitud de inicio de sesión de un usuario.
   *
   * <p>Autentica al usuario usando las credenciales proporcionadas. Si la autenticación es exitosa,
   * recupera la entidad completa del usuario y genera un token JWT.
   *
   * @param user Objeto User que contiene el nombre de usuario y la contraseña para la
   *     autenticación.
   * @return AuthResponseDTO que contiene el JWT generado.
   * @throws NoSuchElementException Si el usuario es autenticado pero no se encuentra en la base de
   *     datos (situación inusual).
   */
  public AuthResponseDto login(UserEntity user) {
    // 1. Intenta autenticar al usuario usando el AuthenticationManager
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

    // TODO: Excepcion personalizada
    // 2. Si la autenticación es exitosa, busca el usuario completo en la DB
    UserEntity userEntity =
        userRepo
            .findByUsername(user.getUsername())
            .orElseThrow(() -> new NoSuchElementException("User not found"));

    // 3. Genera el JWT
    String accessToken = jwtService.generateAccessToken(userEntity);

    // 4. Crea el AuthResponseDTO y lo devuelve en el DTO de respuesta
    return new AuthResponseDto(
        accessToken,
        jwtService.getAccessTokenExpiresIn(),
        jwtService.generateRefreshToken(userEntity),
        null);
  }

  /**
   * Registra un nuevo usuario en la aplicación.
   *
   * <p>Asigna el rol predefinido (con ID 2), codifica la contraseña y establece la fecha de
   * creación antes de guardar el usuario en la base de datos.
   *
   * @param user Objeto User con los datos del nuevo usuario a registrar.
   * @return La entidad User guardada en la base de datos.
   * @throws NoSuchElementException Si el Rol por defecto (ID 2) no se encuentra en la base de
   *     datos.
   */
  public UserEntity register(UserEntity user) {
    // 1. Busca el rol por defecto (asumido como ID 2)
    // TODO: excepcion personalizada
    RolEntity rol =
        rolRepo
            .findByRolName("USER")
            .orElseThrow(() -> new NoSuchElementException("Rol not found"));

    LocalDateTime now = LocalDateTime.now();

    // 2. Codifica la contraseña y establece los datos por defecto
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setCreateDate(now);
    user.setAccountExpirationDate(now.plusYears(2));
    user.setIsLocked(false);
    user.setCredentialsExpirationDate(now.plusYears(1));
    user.setIsEnabled(true);
    user.setUserRol(rol);

    // 3. Guarda el nuevo usuario en la base de datos
    return userRepo.save(user);
  }

  /**
   * Procesa la renovación de tokens utilizando el Refresh Token proporcionado. Genera un nuevo
   * Access Token y devuelve el nuevo par (o mantiene el mismo Refresh Token).
   *
   * @param refreshToken El Refresh Token recibido del cliente.
   * @return AuthResponseDTO con el nuevo Access Token y el Refresh Token.
   */
  public AuthResponseDto refreshToken(String refreshToken) {

    // 1. Validar y obtener el username del Refresh Token
    final String username;
    try {
      username = jwtService.getUsernameFromRefreshToken(refreshToken);
    } catch (JwtException e) {
      // Captura si el token es inválido (firma, formato, etc.)
      throw new IllegalArgumentException("Refresh Token inválido: " + e.getMessage());
    }

    // 2. Cargar detalles del usuario y verificar la validez final del token
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
      // Captura si el token ha expirado o no pertenece al usuario cargado
      throw new IllegalArgumentException("Refresh Token expirado o no válido para el usuario.");
    }

    // 3. Generar un NUEVO Access Token
    UserEntity userEntity = (UserEntity) userDetails; // Castear al tipo de entidad si es necesario

    String newAccessToken = jwtService.generateAccessToken(userEntity);

    // 4. Devolver la respuesta con el nuevo Access Token
    return AuthResponseDto.builder()
        .accessToken(newAccessToken)
        .expiresIn(jwtService.getAccessTokenExpiresIn())
        .refreshToken(refreshToken) // Se mantiene el Refresh Token actual (sin rotación)
        .build();
  }
}
