package org.vedruna.futurjob.controller.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.web.context.request.WebRequest;

class HandlerSecurityExControllerTest {

  private HandlerSecurityExController handler;
  private WebRequest request;

  @BeforeEach
  void setUp() {
    handler = new HandlerSecurityExController();
    request = mock(WebRequest.class);
  }

  @Test
  void testHandleAccessDenied() {
    AccessDeniedException ex = new AccessDeniedException("Access is denied");
    ResponseEntity<Object> response = handler.handleAccessDenied(ex, request);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(response.getBody() instanceof ProblemDetail);
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertEquals("AccessDeniedException: Access Denied", pd.getTitle());
    assertEquals("denied", pd.getProperties().get("access"));
  }

  @Test
  void testHandleBadCredentials() {
    BadCredentialsException ex = new BadCredentialsException("Bad credentials");
    ResponseEntity<Object> response = handler.handleBadCredentials(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("BadCredentialsException: Invalid Credentials", pd.getTitle());
    assertEquals("Invalid credentials", pd.getProperties().get("error"));
  }

  @Test
  void testHandleUsernameNotFound_withMessage() {
    UsernameNotFoundException ex = new UsernameNotFoundException("Custom user not found");
    ResponseEntity<Object> response = handler.handleUsernameNotFound(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Custom user not found", pd.getDetail());
  }

  @Test
  void testHandleUsernameNotFound_withEmptyMessage() {
    UsernameNotFoundException ex = new UsernameNotFoundException("   ");
    ResponseEntity<Object> response = handler.handleUsernameNotFound(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("The requested user could not be found.", pd.getDetail());
  }

  @Test
  void testHandleUsernameNotFound_withNullMessage() {
    UsernameNotFoundException ex = mock(UsernameNotFoundException.class);
    when(ex.getMessage()).thenReturn(null);
    ResponseEntity<Object> response = handler.handleUsernameNotFound(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("The requested user could not be found.", pd.getDetail());
  }

  @Test
  void testHandleLocked() {
    LockedException ex = new LockedException("User account is locked");
    ResponseEntity<Object> response = handler.handleLocked(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("LockedException: Account Locked", pd.getTitle());
    assertEquals("locked", pd.getProperties().get("accountStatus"));
  }

  @Test
  void testHandleDisabled() {
    DisabledException ex = new DisabledException("User is disabled");
    ResponseEntity<Object> response = handler.handleDisabled(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("DisabledException: Account Disabled", pd.getTitle());
    assertEquals("disabled", pd.getProperties().get("accountStatus"));
  }

  @Test
  void testHandleAccountExpired() {
    AccountExpiredException ex = new AccountExpiredException("Account expired");
    ResponseEntity<Object> response = handler.handleAccountExpired(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("AccountExpiredException: Account Expired", pd.getTitle());
    assertEquals("expired", pd.getProperties().get("accountStatus"));
  }

  @Test
  void testHandleCredentialsExpired() {
    CredentialsExpiredException ex = new CredentialsExpiredException("Credentials expired");
    ResponseEntity<Object> response = handler.handleCredentialsExpired(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("CredentialsExpiredException: Credentials Expired", pd.getTitle());
    assertEquals("expired", pd.getProperties().get("credentialsStatus"));
  }

  @Test
  void testHandleAccountStatus_withMessage() {
    AccountStatusException ex = new LockedException("Status error");
    ResponseEntity<Object> response = handler.handleAccountStatus(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Status error", pd.getDetail());
  }

  @Test
  void testHandleAccountStatus_withBlankMessage() {
    AccountStatusException ex = new LockedException("  ");
    ResponseEntity<Object> response = handler.handleAccountStatus(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "Authentication failed due to the current status of the user account.", pd.getDetail());
  }

  @Test
  void testHandleAccountStatus_withNullMessage() {
    AccountStatusException ex = mock(AccountStatusException.class);
    when(ex.getMessage()).thenReturn(null);
    ResponseEntity<Object> response = handler.handleAccountStatus(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "Authentication failed due to the current status of the user account.", pd.getDetail());
  }

  @Test
  void testHandleInsufficientAuthentication() {
    InsufficientAuthenticationException ex =
        new InsufficientAuthenticationException("Full authentication required");
    ResponseEntity<Object> response = handler.handleInsufficientAuthentication(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("InsufficientAuthenticationException: Insufficient Authentication", pd.getTitle());
    assertEquals("insufficient", pd.getProperties().get("auth"));
  }

  @Test
  void testHandleAuthenticationCredentialsNotFound() {
    AuthenticationCredentialsNotFoundException ex =
        new AuthenticationCredentialsNotFoundException("No credentials");
    ResponseEntity<Object> response = handler.handleAuthenticationCredentialsNotFound(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "AuthenticationCredentialsNotFoundException: Credentials Not Found", pd.getTitle());
    assertEquals("credentials_not_found", pd.getProperties().get("auth"));
  }

  @Test
  void testHandleJwtException() {
    JwtException ex = new JwtException("Token expired or corrupted");
    ResponseEntity<Object> response = handler.handleJwtException(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("JwtException: Invalid or Expired Token", pd.getTitle());
    assertEquals("invalid_or_expired", pd.getProperties().get("token"));
  }

  @Test
  void testHandleAuthentication_withMessage() {
    AuthenticationException ex = new AuthenticationException("Auth failed") {};
    ResponseEntity<Object> response = handler.handleAuthentication(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Auth failed", pd.getDetail());
  }

  @Test
  void testHandleAuthentication_withBlankMessage() {
    AuthenticationException ex = new AuthenticationException("") {};
    ResponseEntity<Object> response = handler.handleAuthentication(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("An authentication error occurred while processing the request.", pd.getDetail());
  }

  @Test
  void testHandleAuthentication_withNullMessage() {
    AuthenticationException ex = mock(AuthenticationException.class);
    when(ex.getMessage()).thenReturn(null);
    ResponseEntity<Object> response = handler.handleAuthentication(ex, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("An authentication error occurred while processing the request.", pd.getDetail());
  }
}
