package org.vedruna.futurjob.persistance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Entidad que representa un usuario y sus detalles de seguridad (implementa UserDetails). */
@Data
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  Integer userId;

  @Column(name = "username", unique = true, nullable = false)
  String username;

  @Column(name = "email", unique = true, nullable = false)
  String email;

  @Column(name = "password", nullable = false)
  String password;

  @Column(name = "create_date", nullable = false)
  LocalDateTime createDate;

  @Column(name = "account_expiration_date")
  LocalDateTime accountExpirationDate;

  @Column(name = "is_locked")
  Boolean isLocked;

  @Column(name = "credentials_expiration_date")
  LocalDateTime credentialsExpirationDate;

  @Column(name = "is_enabled")
  Boolean isEnabled;

  // IMPORTANTE EAGER para que se cargue el rol en la consulta
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "roles_rol_id", referencedColumnName = "rol_id")
  RolEntity userRol;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Si el rol es nulo (aunque esto debería ser prevenido por la lógica de negocio/BD)
    if (this.userRol == null || this.userRol.getRolName() == null) {
      return Collections.emptyList();
    }

    // Mapea el nombre del rol a una autoridad de Spring Security
    // Se añade el prefijo "ROLE_" como buena práctica de Spring Security.
    String authorityName = "ROLE_" + this.userRol.getRolName().toUpperCase();

    return List.of(new SimpleGrantedAuthority(authorityName));
  }

  /**
   * Indica si la cuenta del usuario ha expirado.
   *
   * @return true si la cuenta es válida (nunca expira en esta implementación).
   */
  @Override
  public boolean isAccountNonExpired() {
    return LocalDateTime.now().isBefore(accountExpirationDate);
  }

  /**
   * Indica si la cuenta del usuario está bloqueada.
   *
   * @return true si la cuenta no está bloqueada (nunca se bloquea en esta implementación).
   */
  @Override
  public boolean isAccountNonLocked() {
    return !isLocked;
  }

  /**
   * Indica si las credenciales (contraseña) del usuario han expirado.
   *
   * @return true si las credenciales son válidas (nunca expiran en esta implementación).
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return LocalDateTime.now().isBefore(credentialsExpirationDate);
  }

  /**
   * Indica si el usuario está habilitado o deshabilitado.
   *
   * @return true si el usuario está habilitado (siempre habilitado en esta implementación).
   */
  @Override
  public boolean isEnabled() {
    return isEnabled;
  }
}
