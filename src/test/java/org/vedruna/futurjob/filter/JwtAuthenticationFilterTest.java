package org.vedruna.futurjob.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.vedruna.futurjob.service.impl.JwtServiceImpl;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtServiceImpl jwtService;

  @Mock private UserDetailsService userDetailsService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtAuthenticationFilter jwtFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void testDoFilterInternal_noAuthorizationHeader() throws ServletException, IOException {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_headerNotStartingWithBearer() throws ServletException, IOException {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_validToken_authenticatesSuccessfully()
      throws ServletException, IOException {
    String token = "valid-token";
    String username = "john";
    UserDetails userDetails = new User(username, "password", Collections.emptyList());

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromAccessToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isAccessTokenValid(token, userDetails)).thenReturn(true);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
  }

  @Test
  void testDoFilterInternal_alreadyAuthenticated() throws ServletException, IOException {
    String token = "valid-token";
    String username = "john";

    UsernamePasswordAuthenticationToken existingAuth =
        new UsernamePasswordAuthenticationToken("alreadyAuth", null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromAccessToken(token)).thenReturn(username);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);
    assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_tokenInvalid() throws ServletException, IOException {
    String token = "invalid-token";
    String username = "john";
    UserDetails userDetails = new User(username, "password", Collections.emptyList());

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromAccessToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isAccessTokenValid(token, userDetails)).thenReturn(false);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_nullUsername() throws ServletException, IOException {
    String token = "token-without-sub";

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromAccessToken(token)).thenReturn(null);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_jwtException() throws ServletException, IOException {
    String token = "malformed-token";

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(request.getRequestURI()).thenReturn("/api/test");
    when(jwtService.getUsernameFromAccessToken(token))
        .thenThrow(new JwtException("Signature failed"));

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_usernameNotFoundException() throws ServletException, IOException {
    String token = "valid-token";
    String username = "unknownUser";

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromAccessToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username))
        .thenThrow(new UsernameNotFoundException("User not in DB"));

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_unexpectedException() throws ServletException, IOException {
    String token = "valid-token";

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromAccessToken(token))
        .thenThrow(new RuntimeException("Database down"));

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
