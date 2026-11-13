package org.vedruna.futurjob.service;

import io.jsonwebtoken.Claims;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.vedruna.futurjob.persistance.model.UserEntity;

/**
 * Interfaz que define las operaciones para la creación, extracción y validación de JSON Web Tokens
 * (JWT) de acceso y de refresco.
 */
public interface JwtService {

  /** Convierte una clave secreta en formato Base64 a un objeto SecretKey. */
  SecretKey getKey(String secretKey);

  /** Genera un Access Token para el usuario especificado. */
  String generateAccessToken(UserEntity user);

  /** Genera un Refresh Token para el usuario especificado. */
  String generateRefreshToken(UserEntity user);

  /** Extrae un claim específico del token mediante una función resolutora. */
  <T> T getClaim(String token, Function<Claims, T> claimsResolver, String secretKey);

  /** Obtiene el nombre de usuario (Subject) contenido en un Access Token. */
  String getUsernameFromAccessToken(String token);

  /** Devuelve el tiempo de vida del Access Token en segundos. */
  Long getAccessTokenExpiresIn();

  /** Valida si el Access Token es válido y pertenece al usuario. */
  boolean isAccessTokenValid(String token, UserDetails userDetails);

  /** Obtiene el nombre de usuario (Subject) contenido en un Refresh Token. */
  String getUsernameFromRefreshToken(String token);

  /** Devuelve el tiempo de vida del Refresh Token en segundos. */
  Long getRefreshTokenExpiresIn();

  /** Valida si el Refresh Token es válido y pertenece al usuario. */
  boolean isRefreshTokenValid(String token, UserDetails userDetails);
}
