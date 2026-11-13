package org.vedruna.futurjob.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.JwtException;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vedruna.futurjob.controller.dto.authdto.AuthResponseDto;
import org.vedruna.futurjob.persistance.model.RolEntity;
import org.vedruna.futurjob.persistance.model.UserEntity;
import org.vedruna.futurjob.persistance.repository.RolRepository;
import org.vedruna.futurjob.persistance.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private UserRepository userRepo;

  @Mock private RolRepository rolRepo;

  @Mock private JwtServiceImpl jwtService;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private UserDetailsService userDetailsService;

  @InjectMocks private AuthServiceImpl authService;

  @Test
  void testLogin_success() {
    UserEntity inputUser = new UserEntity();
    inputUser.setUsername("testuser");
    inputUser.setPassword("plainPassword");

    UserEntity dbUser = new UserEntity();
    dbUser.setUserId(1);
    dbUser.setUsername("testuser");

    when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(dbUser));
    when(jwtService.generateAccessToken(dbUser)).thenReturn("access-token-abc");
    when(jwtService.getAccessTokenExpiresIn()).thenReturn(3600L);
    when(jwtService.generateRefreshToken(dbUser)).thenReturn("refresh-token-xyz");

    AuthResponseDto response = authService.login(inputUser);

    assertNotNull(response);
    assertEquals("access-token-abc", response.getAccessToken());
    assertEquals(3600L, response.getExpiresIn());
    assertEquals("refresh-token-xyz", response.getRefreshToken());

    verify(authenticationManager)
        .authenticate(new UsernamePasswordAuthenticationToken("testuser", "plainPassword"));
    verify(userRepo).findByUsername("testuser");
  }

  @Test
  void testLogin_userNotFoundInDb() {
    UserEntity inputUser = new UserEntity();
    inputUser.setUsername("nonexistent");
    inputUser.setPassword("password");

    when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

    NoSuchElementException ex =
        assertThrows(NoSuchElementException.class, () -> authService.login(inputUser));

    assertEquals("User not found", ex.getMessage());
    verify(authenticationManager)
        .authenticate(new UsernamePasswordAuthenticationToken("nonexistent", "password"));
  }

  @Test
  void testRegister_success() {
    UserEntity inputUser = new UserEntity();
    inputUser.setUsername("newuser");
    inputUser.setPassword("rawPassword");

    RolEntity defaultRole = new RolEntity();
    defaultRole.setUserId(2);
    defaultRole.setRolName("USER");

    when(rolRepo.findByRolName("USER")).thenReturn(Optional.of(defaultRole));
    when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
    when(userRepo.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity savedUser = authService.register(inputUser);

    assertNotNull(savedUser);
    assertEquals("encodedPassword", savedUser.getPassword());
    assertEquals(defaultRole, savedUser.getUserRol());
    assertFalse(savedUser.getIsLocked());
    assertTrue(savedUser.getIsEnabled());
    assertNotNull(savedUser.getCreateDate());
    assertNotNull(savedUser.getAccountExpirationDate());
    assertNotNull(savedUser.getCredentialsExpirationDate());

    verify(rolRepo).findByRolName("USER");
    verify(passwordEncoder).encode("rawPassword");
    verify(userRepo).save(inputUser);
  }

  @Test
  void testRegister_roleNotFound() {
    UserEntity inputUser = new UserEntity();
    inputUser.setUsername("newuser");

    when(rolRepo.findByRolName("USER")).thenReturn(Optional.empty());

    NoSuchElementException ex =
        assertThrows(NoSuchElementException.class, () -> authService.register(inputUser));

    assertEquals("Rol not found", ex.getMessage());
  }

  @Test
  void testRefreshToken_success() {
    String refreshToken = "valid-refresh-token";
    String username = "john";

    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(1);
    userEntity.setUsername(username);

    when(jwtService.getUsernameFromRefreshToken(refreshToken)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userEntity);
    when(jwtService.isRefreshTokenValid(refreshToken, userEntity)).thenReturn(true);
    when(jwtService.generateAccessToken(userEntity)).thenReturn("new-access-token");
    when(jwtService.getAccessTokenExpiresIn()).thenReturn(3600L);

    AuthResponseDto response = authService.refreshToken(refreshToken);

    assertNotNull(response);
    assertEquals("new-access-token", response.getAccessToken());
    assertEquals(3600L, response.getExpiresIn());
    assertEquals(refreshToken, response.getRefreshToken());
  }

  @Test
  void testRefreshToken_invalidJwtException() {
    String refreshToken = "corrupted-token";

    when(jwtService.getUsernameFromRefreshToken(refreshToken))
        .thenThrow(new JwtException("Token is malformed"));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(refreshToken));

    assertTrue(ex.getMessage().contains("Refresh Token inválido: Token is malformed"));
  }

  @Test
  void testRefreshToken_tokenNotValidForUser() {
    String refreshToken = "expired-token";
    String username = "john";

    UserEntity userEntity = new UserEntity();
    userEntity.setUsername(username);

    when(jwtService.getUsernameFromRefreshToken(refreshToken)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userEntity);
    when(jwtService.isRefreshTokenValid(refreshToken, userEntity)).thenReturn(false);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(refreshToken));

    assertTrue(ex.getMessage().contains("Refresh Token expirado o no válido para el usuario."));
  }
}
