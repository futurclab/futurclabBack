package org.vedruna.futurjob.controller.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.vedruna.futurjob.persistance.exception.UserNotFoundException;

class HandlerExceptionControllerTest {

  private HandlerExceptionController handler;
  private WebRequest request;

  @BeforeEach
  void setUp() {
    handler = new HandlerExceptionController();
    request = mock(WebRequest.class);
  }

  @Test
  void testHandleMethodArgumentTypeMismatch_withRequiredType() {
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getName()).thenReturn("age");
    when(ex.getValue()).thenReturn("twenty");
    doReturn(Integer.class).when(ex).getRequiredType();

    ResponseEntity<Object> response = handler.handleMethodArgumentTypeMismatch(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertTrue(pd.getDetail().contains("must be of type 'Integer'"));
  }

  @Test
  void testHandleMethodArgumentTypeMismatch_withNullRequiredType() {
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getName()).thenReturn("unknown");
    when(ex.getValue()).thenReturn("val");
    doReturn(null).when(ex).getRequiredType();

    ResponseEntity<Object> response = handler.handleMethodArgumentTypeMismatch(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertTrue(pd.getDetail().contains("must be of type 'a different type'"));
  }

  @Test
  void testHandleUnexpectedType() {
    UnexpectedTypeException ex = new UnexpectedTypeException("Unexpected type");
    ResponseEntity<Object> response = handler.handleUnexpectedType(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("UnexpectedTypeException: Unexpected Validation Type", pd.getTitle());
  }

  @Test
  void testHandleIllegalArgument_withMessage() {
    IllegalArgumentException ex = new IllegalArgumentException("Business rule violated");
    ResponseEntity<Object> response = handler.handleIllegalArgument(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Business rule violated", pd.getDetail());
  }

  @Test
  void testHandleIllegalArgument_withNullMessage() {
    IllegalArgumentException ex = new IllegalArgumentException((String) null);
    ResponseEntity<Object> response = handler.handleIllegalArgument(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "The request argument is invalid due to a violation of business rules.", pd.getDetail());
  }

  @Test
  void testHandleUserNotFound_withMessage() {
    UserNotFoundException ex = new UserNotFoundException();
    ResponseEntity<Object> response = handler.handleUserNotFound(ex, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("User not found", pd.getDetail());
  }

  @Test
  void testHandleUserNotFound_withNullMessage() {
    UserNotFoundException ex = mock(UserNotFoundException.class);
    when(ex.getMessage()).thenReturn(null);
    ResponseEntity<Object> response = handler.handleUserNotFound(ex, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("The requested film was not found.", pd.getDetail());
  }

  @Test
  void testHandleEmptyResultDataAccess() {
    EmptyResultDataAccessException ex = new EmptyResultDataAccessException("Not found", 1);
    ResponseEntity<Object> response = handler.handleEmptyResultDataAccess(ex, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("EmptyResultDataAccessException: Resource Not Found", pd.getTitle());
  }

  @Test
  void testHandleDataIntegrityViolation() {
    DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate key");
    ResponseEntity<Object> response = handler.handleDataIntegrityViolation(ex, request);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("DataIntegrityViolationException: Data Conflict", pd.getTitle());
  }

  @Test
  void testHandleHttpMessageNotReadable_withRootCause() {
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    Throwable rootCause = new RuntimeException("Syntax error at line 1");
    when(ex.getRootCause()).thenReturn(rootCause);

    ResponseEntity<Object> response =
        handler.handleHttpMessageNotReadable(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Syntax error at line 1", pd.getDetail());
  }

  @Test
  void testHandleHttpMessageNotReadable_withNullRootCause() {
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getRootCause()).thenReturn(null);

    ResponseEntity<Object> response =
        handler.handleHttpMessageNotReadable(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertTrue(pd.getDetail().contains("The request body could not be read or is malformed"));
  }

  @Test
  void testHandleConstraintViolation_withDuplicates() {
    ConstraintViolation<?> cv1 = mock(ConstraintViolation.class);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("email");
    when(cv1.getPropertyPath()).thenReturn(path1);
    when(cv1.getMessage()).thenReturn("must be valid email");

    ConstraintViolation<?> cv2 = mock(ConstraintViolation.class);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("email");
    when(cv2.getPropertyPath()).thenReturn(path2);
    when(cv2.getMessage()).thenReturn("cannot be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>(Arrays.asList(cv1, cv2));
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ResponseEntity<Object> response = handler.handleConstraintViolation(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) pd.getProperties().get("errors");
    assertNotNull(errors);
    assertTrue(errors.containsKey("email"));
  }

  @Test
  void testHandleMethodArgumentNotValid_withNullMessageAndDuplicates() {
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);

    FieldError fe1 = new FieldError("user", "username", "cannot be empty");
    FieldError fe2 = new FieldError("user", "username", "must have min 3 chars");
    FieldError fe3 = new FieldError("user", "password", null);

    when(bindingResult.getFieldErrors()).thenReturn(List.of(fe1, fe2, fe3));
    when(ex.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) pd.getProperties().get("errors");
    assertNotNull(errors);
    assertEquals("cannot be empty", errors.get("username"));
    assertEquals("Invalid value", errors.get("password"));
  }

  @Test
  void testHandleValidation_withMessage() {
    ValidationException ex = new ValidationException("Validation failed for input");
    ResponseEntity<Object> response = handler.handleValidation(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Validation failed for input", pd.getDetail());
  }

  @Test
  void testHandleValidation_withBlankMessage() {
    ValidationException ex = new ValidationException("   ");
    ResponseEntity<Object> response = handler.handleValidation(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "The request data is invalid. One or more validation rules were violated.", pd.getDetail());
  }

  @Test
  void testHandleValidation_withNullMessage() {
    ValidationException ex = new ValidationException((String) null);
    ResponseEntity<Object> response = handler.handleValidation(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "The request data is invalid. One or more validation rules were violated.", pd.getDetail());
  }

  @Test
  void testHandleRollBack() {
    RollbackException ex = new RollbackException("Transaction rollback");
    ResponseEntity<Object> response = handler.handleRollBack(ex, request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("RollbackException: Internal Server Error", pd.getTitle());
  }

  @Test
  void testHandleHttpRequestMethodNotSupported_withSupportedMethodsAndMessage() {
    HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
    when(ex.getMessage()).thenReturn("Method POST not supported");
    when(ex.getSupportedHttpMethods()).thenReturn(Set.of(HttpMethod.GET, HttpMethod.PUT));

    HttpHeaders headers = new HttpHeaders();
    ResponseEntity<Object> response =
        handler.handleHttpRequestMethodNotSupported(
            ex, headers, HttpStatus.METHOD_NOT_ALLOWED, request);

    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Method POST not supported", pd.getDetail());
    assertNotNull(headers.getAllow());
    assertTrue(headers.getAllow().contains(HttpMethod.GET));
    assertTrue(headers.getAllow().contains(HttpMethod.PUT));
    assertNotNull(pd.getProperties().get("allowedMethods"));
  }

  @Test
  void testHandleHttpRequestMethodNotSupported_withNullMethodsAndBlankMessage() {
    HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
    when(ex.getMessage()).thenReturn("  ");
    when(ex.getSupportedHttpMethods()).thenReturn(null);

    HttpHeaders headers = new HttpHeaders();
    ResponseEntity<Object> response =
        handler.handleHttpRequestMethodNotSupported(
            ex, headers, HttpStatus.METHOD_NOT_ALLOWED, request);

    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "The HTTP method used for this resource is not supported. Please check the allowed methods.",
        pd.getDetail());
  }

  @Test
  void testHandleHttpRequestMethodNotSupported_withNullMessage() {
    HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
    when(ex.getMessage()).thenReturn(null);
    when(ex.getSupportedHttpMethods()).thenReturn(null);

    HttpHeaders headers = new HttpHeaders();
    ResponseEntity<Object> response =
        handler.handleHttpRequestMethodNotSupported(
            ex, headers, HttpStatus.METHOD_NOT_ALLOWED, request);

    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "The HTTP method used for this resource is not supported. Please check the allowed methods.",
        pd.getDetail());
  }

  @Test
  void testHandleHttpRequestMethodNotSupported_withEmptySupportedMethods() {
    HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
    when(ex.getMessage()).thenReturn("Method DELETE not supported");
    when(ex.getSupportedHttpMethods()).thenReturn(Collections.emptySet());

    HttpHeaders headers = new HttpHeaders();
    ResponseEntity<Object> response =
        handler.handleHttpRequestMethodNotSupported(
            ex, headers, HttpStatus.METHOD_NOT_ALLOWED, request);

    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("Method DELETE not supported", pd.getDetail());
    assertTrue(headers.getAllow().isEmpty());
    assertNull(pd.getProperties() != null ? pd.getProperties().get("allowedMethods") : null);
  }

  @Test
  void testHandlePropertyReference_withMessage() {
    PropertyReferenceException ex = mock(PropertyReferenceException.class);
    when(ex.getMessage()).thenReturn("No property found");
    when(ex.getPropertyName()).thenReturn("nonexistentProp");

    ResponseEntity<Object> response = handler.handlePropertyReference(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals("No property found", pd.getDetail());
    assertEquals("nonexistentProp", pd.getProperties().get("invalidProperty"));
  }

  @Test
  void testHandlePropertyReference_withBlankMessage() {
    PropertyReferenceException ex = mock(PropertyReferenceException.class);
    when(ex.getMessage()).thenReturn("");
    when(ex.getPropertyName()).thenReturn("prop");

    ResponseEntity<Object> response = handler.handlePropertyReference(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "The requested property for sorting or searching does not exist on the resource.",
        pd.getDetail());
  }

  @Test
  void testHandlePropertyReference_withNullMessage() {
    PropertyReferenceException ex = mock(PropertyReferenceException.class);
    when(ex.getMessage()).thenReturn(null);
    when(ex.getPropertyName()).thenReturn("prop");

    ResponseEntity<Object> response = handler.handlePropertyReference(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail pd = (ProblemDetail) response.getBody();
    assertNotNull(pd);
    assertEquals(
        "The requested property for sorting or searching does not exist on the resource.",
        pd.getDetail());
  }
}
