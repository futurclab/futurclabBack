package org.vedruna.futurjob.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.vedruna.futurjob.config.AuthProperties;
import org.vedruna.futurjob.persistance.model.UserEntity;

class JwtServiceImplTest {

  private static final String ACCESS_SECRET = "aM3I6FjqF2L3H1wQvGRb9J7Fc6OgezCpnMjMrxECJHk=";
  private static final String REFRESH_SECRET = "DNfQXVPMvyL5TKLZ+ZCmhRAxixdbtz/IRoPKgXJMJGQ=";

  private AuthProperties authProperties;
  private JwtServiceImpl jwtService;

  @BeforeEach
  void setUp() {
    authProperties = new AuthProperties();
    authProperties.setAccessTokenSecretKey(ACCESS_SECRET);
    authProperties.setAccessTokenExpiration(180000L); // 3 minutes
    authProperties.setRefreshTokenSecretKey(REFRESH_SECRET);
    authProperties.setRefreshTokenExpiration(604800000L); // 7 days

    jwtService = new JwtServiceImpl(authProperties);
  }

  @Test
  void testGetKey() {
    SecretKey key = jwtService.getKey(ACCESS_SECRET);
    assertNotNull(key);
    assertEquals("HmacSHA256", key.getAlgorithm());
  }

  @Test
  void testGenerateAndValidateAccessToken() {
    UserEntity user = new UserEntity();
    user.setUsername("testuser");
    user.setEmail("test@example.com");

    String token = jwtService.generateAccessToken(user);
    assertNotNull(token);

    String username = jwtService.getUsernameFromAccessToken(token);
    assertEquals("testuser", username);

    String emailClaim =
        jwtService.getClaim(token, claims -> claims.get("email", String.class), ACCESS_SECRET);
    assertEquals("test@example.com", emailClaim);

    assertEquals(180L, jwtService.getAccessTokenExpiresIn());

    UserDetails matchingUser =
        User.withUsername("testuser").password("password").roles("USER").build();
    assertTrue(jwtService.isAccessTokenValid(token, matchingUser));

    UserDetails differentUser =
        User.withUsername("otheruser").password("password").roles("USER").build();
    assertFalse(jwtService.isAccessTokenValid(token, differentUser));
  }

  @Test
  void testGenerateAndValidateRefreshToken() {
    UserEntity user = new UserEntity();
    user.setUsername("refreshuser");
    user.setEmail("refresh@example.com");

    String token = jwtService.generateRefreshToken(user);
    assertNotNull(token);

    String username = jwtService.getUsernameFromRefreshToken(token);
    assertEquals("refreshuser", username);

    assertEquals(604800L, jwtService.getRefreshTokenExpiresIn());

    UserDetails matchingUser =
        User.withUsername("refreshuser").password("pass").roles("USER").build();
    assertTrue(jwtService.isRefreshTokenValid(token, matchingUser));

    UserDetails differentUser =
        User.withUsername("otheruser").password("pass").roles("USER").build();
    assertFalse(jwtService.isRefreshTokenValid(token, differentUser));
  }

  @Test
  void testInvalidTokenThrowsException() {
    assertThrows(
        JwtException.class, () -> jwtService.getUsernameFromAccessToken("invalid.malformed.token"));
    assertThrows(
        JwtException.class,
        () -> jwtService.getUsernameFromRefreshToken("invalid.malformed.token"));
  }

  @Test
  void testExpiredAccessToken() {
    // Set expiration to negative value so token is instantly expired
    authProperties.setAccessTokenExpiration(-1000L);
    UserEntity user = new UserEntity();
    user.setUsername("expireduser");
    user.setEmail("expired@example.com");

    String token = jwtService.generateAccessToken(user);

    UserDetails userDetails =
        User.withUsername("expireduser").password("pass").roles("USER").build();

    assertThrows(JwtException.class, () -> jwtService.isAccessTokenValid(token, userDetails));
  }

  @Test
  void testExpiredRefreshToken() {
    // Set expiration to negative value so token is instantly expired
    authProperties.setRefreshTokenExpiration(-1000L);
    UserEntity user = new UserEntity();
    user.setUsername("expiredrefreshuser");

    String token = jwtService.generateRefreshToken(user);

    UserDetails userDetails =
        User.withUsername("expiredrefreshuser").password("pass").roles("USER").build();

    assertThrows(JwtException.class, () -> jwtService.isRefreshTokenValid(token, userDetails));
  }

  @Test
  void testIsAccessTokenValid_matchingUserButExpiredDate_returnsFalse() {
    JwtServiceImpl spyJwt = spy(jwtService);
    UserDetails userDetails = User.withUsername("testuser").password("pass").roles("USER").build();

    doAnswer(
            invocation -> {
              @SuppressWarnings("unchecked")
              Function<Claims, ?> resolver = invocation.getArgument(1, Function.class);
              Claims mockClaims = mock(Claims.class);
              when(mockClaims.getSubject()).thenReturn("testuser");
              when(mockClaims.getExpiration())
                  .thenReturn(new Date(System.currentTimeMillis() - 50000));
              return resolver.apply(mockClaims);
            })
        .when(spyJwt)
        .getClaim(anyString(), any(), anyString());

    assertFalse(spyJwt.isAccessTokenValid("dummy-token", userDetails));
  }

  @Test
  void testIsRefreshTokenValid_matchingUserButExpiredDate_returnsFalse() {
    JwtServiceImpl spyJwt = spy(jwtService);
    UserDetails userDetails =
        User.withUsername("refreshuser").password("pass").roles("USER").build();

    doAnswer(
            invocation -> {
              @SuppressWarnings("unchecked")
              Function<Claims, ?> resolver = invocation.getArgument(1, Function.class);
              Claims mockClaims = mock(Claims.class);
              when(mockClaims.getSubject()).thenReturn("refreshuser");
              when(mockClaims.getExpiration())
                  .thenReturn(new Date(System.currentTimeMillis() - 50000));
              return resolver.apply(mockClaims);
            })
        .when(spyJwt)
        .getClaim(anyString(), any(), anyString());

    assertFalse(spyJwt.isRefreshTokenValid("dummy-token", userDetails));
  }
}
