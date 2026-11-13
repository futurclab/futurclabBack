package org.vedruna.futurjob.controller.handler;

import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.vedruna.futurjob.persistance.exception.UserNotFoundException;

/**
 * Controlador de Asesoramiento REST (Global Exception Handler) para gestionar las excepciones
 * lanzadas por la aplicación y convertirlas en respuestas HTTP estandarizadas (ProblemDetail).
 *
 * <p>Extiende {@link ResponseEntityExceptionHandler} para manejar las excepciones internas del
 * framework Spring y añade métodos {@code @ExceptionHandler} para capturar excepciones específicas
 * de la aplicación y la persistencia.
 *
 * <p>{@code @AllArgsConstructor}: Anotación de Lombok para generar un constructor con todos los
 * argumentos.
 *
 * <p>{@code @Slf4j}: Anotación de Lombok para habilitar el logging a través del objeto {@code log}.
 *
 * <p>{@code @RestControllerAdvice}: Combina {@code @ControllerAdvice} y {@code @ResponseBody},
 * permitiendo que los métodos de manejo de excepciones devuelvan objetos directamente al cuerpo de
 * la respuesta HTTP.
 */
@AllArgsConstructor
@Slf4j
@RestControllerAdvice
public class HandlerExceptionController extends ResponseEntityExceptionHandler {

  /**
   * Maneja excepciones cuando un argumento de método (como un {@code @RequestParam} o
   * {@code @PathVariable}) no se puede convertir al tipo de dato requerido. Genera una respuesta
   * 400 Bad Request.
   *
   * @param ex La excepción {@code MethodArgumentTypeMismatchException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y detalles sobre el error de tipo.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la
    // excepcion ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            String.format(
                "The parameter '%s' with value '%s' must be of type '%s'.",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null
                    ? ex.getRequiredType().getSimpleName()
                    : "a different type"));
    // Establecemos el título del error.
    problemDetail.setTitle("MethodArgumentTypeMismatchException: Invalid Argument Type");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/method/annotation/MethodArgumentTypeMismatchException.html"));
    // Registramos el error en el log.
    log.warn("Mismatched Argument Type (400): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado BAD REQUEST.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Maneja excepciones cuando se aplica una anotación de validación a un tipo de dato que no es
   * compatible (por ejemplo, {@code @NotBlank} en un {@code Integer}). Genera una respuesta 400 Bad
   * Request.
   *
   * @param ex La excepción {@code UnexpectedTypeException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y el mensaje de error.
   */
  @ExceptionHandler(UnexpectedTypeException.class)
  protected ResponseEntity<Object> handleUnexpectedType(
      UnexpectedTypeException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la
    // excepcion ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "A type mismatch was encountered during validation processing. Check if validation annotations are applied correctly to the target field's type.");
    // Establecemos el título del error.
    problemDetail.setTitle("UnexpectedTypeException: Unexpected Validation Type");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://jakarta.ee/specifications/bean-validation/3.0/apidocs/jakarta/validation/UnexpectedTypeException.html"));
    // Registramos el error en el log.
    log.warn("Unexpected Type Exception during validation (400): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado BAD REQUEST.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Maneja excepciones lanzadas por la aplicación debido a argumentos que violan reglas de negocio
   * o valores inválidos (e.g., lógica que asegura que una fecha sea posterior a otra). Genera una
   * respuesta 400 Bad Request.
   *
   * @param ex La excepción {@code IllegalArgumentException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y el mensaje de la excepción.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<Object> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la
    // excepcion ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage() != null
                ? ex.getMessage()
                : "The request argument is invalid due to a violation of business rules.");
    // Establecemos el título del error.
    problemDetail.setTitle("IllegalArgumentException: Invalid Business Argument");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/IllegalArgumentException.html"));
    // Registramos el error en el log.
    log.warn("Illegal Argument Exception (400): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado BAD REQUEST.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Maneja la excepción personalizada {@code UserNotFoundException} cuando un usuario solicitado no
   * se encuentra en el sistema. Genera una respuesta 404 Not Found.
   *
   * @param ex La excepción {@code FilmNotFoundException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 404 y el mensaje de la excepción.
   */
  @ExceptionHandler(UserNotFoundException.class)
  protected ResponseEntity<Object> handleUserNotFound(
      UserNotFoundException ex, WebRequest request) {
    // Instanciamos un ProblemDetail mediante un Status y un mensaje detallando la
    // excepcion ocurrida.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage() != null ? ex.getMessage() : "The requested film was not found.");
    // Establecemos el título del error.
    problemDetail.setTitle("UserNotFoundException: User Not Found");
    // Adjuntamos un enlace a la documentación del error (Aquí debería ir la
    // documentación de tu app).
    // Registramos el error en el log.
    log.warn("User Not Found Exception (404): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado NOT FOUND.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  /**
   * Maneja la excepción de Spring Data {@code EmptyResultDataAccessException}, que suele ocurrir
   * tras intentar eliminar una entidad que no existe o al esperar un resultado único de la base de
   * datos que no se encuentra. Genera una respuesta 404 Not Found.
   *
   * @param ex La excepción {@code EmptyResultDataAccessException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 404 y el mensaje de recurso no encontrado.
   */
  @ExceptionHandler(EmptyResultDataAccessException.class)
  protected ResponseEntity<Object> handleEmptyResultDataAccess(
      EmptyResultDataAccessException ex, WebRequest request) {
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "The requested resource was not found in the database. It may have been deleted or the identifier is incorrect.");
    // Establecemos el título del error.
    problemDetail.setTitle("EmptyResultDataAccessException: Resource Not Found");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/dao/EmptyResultDataAccessException.html"));
    // Registramos el error en el log.
    log.warn("Empty Result Data Access Exception (404): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado NOT FOUND.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  /**
   * Maneja la excepción de Spring Data {@code DataIntegrityViolationException}, que ocurre al
   * violar una restricción de la base de datos (e.g., clave única duplicada, violación de clave
   * foránea). Genera una respuesta 409 Conflict.
   *
   * @param ex La excepción {@code DataIntegrityViolationException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 409 y el mensaje de conflicto de datos.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity<Object> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, WebRequest request) {
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "The request could not be completed due to a conflict with existing data or a data constraint. This may be a duplicate unique entry or a foreign key violation.");
    // Establecemos el título del error.
    problemDetail.setTitle("DataIntegrityViolationException: Data Conflict");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/dao/DataIntegrityViolationException.html"));
    // Registramos el error en el log.
    log.warn("Data Integrity Violation (409 Conflict): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado CONFLICT.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  /**
   * Anulación del método de manejo de Spring MVC para excepciones que ocurren cuando el cuerpo de
   * la solicitud JSON no se puede deserializar o está mal formado. Genera una respuesta 400 Bad
   * Request.
   *
   * @param ex La excepción {@code HttpMessageNotReadableException}.
   * @param headers Los encabezados HTTP.
   * @param status El código de estado HTTP (siempre 400 en este caso).
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y detalles sobre el cuerpo mal formado.
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getRootCause() != null
                ? ex.getRootCause().getLocalizedMessage()
                : "The request body could not be read or is malformed. Please check the JSON syntax and data types.");
    // Establecemos el título del error.
    problemDetail.setTitle("HttpMessageNotReadableException: Malformed Body");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/converter/HttpMessageNotReadableException.html"));
    // Registramos el error en el log.
    log.warn("HTTP Message Not Readable (Malformed Body): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado BAD_REQUEST.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Maneja la excepción {@code ConstraintViolationException}, que se lanza cuando las validaciones
   * JSR 380 (Jakarta Validation) fallan en parámetros de métodos anotados con {@code @RequestParam}
   * o {@code @PathVariable} (si la clase está anotada con {@code @Validated}). Genera una respuesta
   * 400 Bad Request.
   *
   * @param ex La excepción {@code ConstraintViolationException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y un mapa de errores por campo.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    // Recolectamos los errores de validación en un mapa.
    Map<String, String> errors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(), // Clave: campo que falló
                    ConstraintViolation::getMessage, // Valor: mensaje de error
                    (existing, replacement) -> existing // Resolver duplicados (mantener el primero)
                    ));
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "One or more constraints were violated. Please check the 'errors' property for details on the invalid fields.");
    // Establecemos el título del error.
    problemDetail.setTitle("ConstraintViolationException: Constraint Violation");
    // Añadimos los errores al ProblemDetail.
    problemDetail.setProperty("errors", errors);
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://jakarta.ee/specifications/bean-validation/3.0/apidocs/jakarta/validation/ConstraintViolationException.html"));
    // Registramos el error en el log.
    log.warn("Constraint Violation Exception (400 Bad Request): {}", ex);

    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Anulación del método de manejo de Spring MVC para excepciones que ocurren cuando la validación
   * JSR 380 (Jakarta Validation) falla en un DTO anotado con {@code @Valid} en el cuerpo de la
   * solicitud (e.g., {@code @RequestBody}). Genera una respuesta 400 Bad Request.
   *
   * @param ex La excepción {@code MethodArgumentNotValidException}.
   * @param headers Los encabezados HTTP.
   * @param status El código de estado HTTP (siempre 400 en este caso).
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y un mapa de errores por campo.
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    // Recolectamos los errores de validación en un mapa.
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField, // Clave: Nombre del campo
                    error ->
                        (error.getDefaultMessage() != null)
                            ? error.getDefaultMessage()
                            : "Invalid value", // Valor: Mensaje de error (con fallback)
                    (existing, replacement) -> existing // Resolver duplicados (mantener el primero)
                    ));
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "The request body contains invalid data. Please correct the fields listed in the 'errors' property.");
    // Establecemos el título del error.
    problemDetail.setTitle("MethodArgumentNotValidException: Validation Error");
    // Añadimos los errores al ProblemDetail.
    problemDetail.setProperty("errors", errors);
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html"));
    // Registramos el error en el log.
    log.warn("Method Argument Not Valid Exception (400 Bad Request): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado BAD_REQUEST.
    return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Maneja la excepción general de validación {@code ValidationException}. Genera una respuesta 400
   * Bad Request.
   *
   * @param ex La excepción {@code ValidationException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y el mensaje de la excepción.
   */
  @ExceptionHandler(ValidationException.class)
  protected ResponseEntity<Object> handleValidation(ValidationException ex, WebRequest request) {
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                ? ex.getMessage()
                : "The request data is invalid. One or more validation rules were violated.");
    // Establecemos el título del error.
    problemDetail.setTitle("ValidationException: Validation Failed");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://jakarta.ee/specifications/bean-validation/3.0/apidocs/jakarta/validation/ValidationException.html"));
    // Registramos el error en el log.
    log.warn("Validation Exception (400 Bad Request): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado BAD_REQUEST.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Maneja excepciones de {@code RollbackException}, que generalmente envuelven una excepción
   * subyacente que causó que la transacción de la base de datos fallara y se revirtiera. Genera una
   * respuesta 500 Internal Server Error.
   *
   * @param ex La excepción {@code RollbackException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 500 y un mensaje genérico de error de transacción.
   */
  @ExceptionHandler(RollbackException.class)
  protected ResponseEntity<Object> handleRollBack(RollbackException ex, WebRequest request) {
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred while processing the transaction. The operation could not be completed.");
    // Establecemos el título del error.
    problemDetail.setTitle("RollbackException: Internal Server Error");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://jakarta.ee/specifications/persistence/3.0/apidocs/jakarta.persistence/jakarta/persistence/rollbackexception"));
    // Registramos el error en el log.
    log.error("Transaction Rollback Failure (500 Internal Error): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado
    // INTERNAL_SERVER_ERROR.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  /**
   * Anulación del método de manejo de Spring MVC para excepciones que ocurren cuando se utiliza un
   * método HTTP no permitido para un recurso específico (e.g., intentar un POST en un endpoint que
   * solo permite GET). Genera una respuesta 405 Method Not Allowed.
   *
   * @param ex La excepción {@code HttpRequestMethodNotSupportedException}.
   * @param headers Los encabezados HTTP.
   * @param status El código de estado HTTP (siempre 405 en este caso).
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 405, incluyendo el encabezado 'Allow' con los
   *     métodos permitidos.
   */
  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                ? ex.getMessage()
                : "The HTTP method used for this resource is not supported. Please check the allowed methods.");
    // Establecemos el título del error.
    problemDetail.setTitle("HttpRequestMethodNotSupportedException: HTTP Method Not Allowed");
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/HttpRequestMethodNotSupportedException.html"));
    // Si el recurso tiene metodos permitidos, establecemos el encabezado 'Allow'.
    if (ex.getSupportedHttpMethods() != null && !ex.getSupportedHttpMethods().isEmpty()) {
      // Establecer el encabezado 'Allow'.
      headers.setAllow(ex.getSupportedHttpMethods());
      // Convertir el Set<HttpMethod> de ex.getSupportedHttpMethods() a un Set<String>
      Set<String> allowedMethodsStrings =
          ex.getSupportedHttpMethods().stream()
              .map(HttpMethod::name) // Tomamos el nombre del método (ej. "GET", "POST")
              .collect(Collectors.toSet());
      // Añadir una propiedad al cuerpo para mayor claridad.
      problemDetail.setProperty("allowedMethods", allowedMethodsStrings);
    }
    // Registramos el error en el log.
    log.warn("HTTP Method Not Supported (405): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado METHOD_NOT_ALLOWED.
    return handleExceptionInternal(
        ex, problemDetail, headers, HttpStatus.METHOD_NOT_ALLOWED, request);
  }

  /**
   * Maneja la excepción de Spring Data {@code PropertyReferenceException}, que ocurre cuando se
   * especifica un nombre de propiedad inválido para la ordenación o paginación (e.g., en el
   * parámetro 'sort' de {@code Pageable}). Genera una respuesta 400 Bad Request.
   *
   * @param ex La excepción {@code PropertyReferenceException}.
   * @param request La solicitud web actual.
   * @return {@code ResponseEntity} con el estado 400 y detalles sobre la propiedad inválida.
   */
  @ExceptionHandler(PropertyReferenceException.class)
  protected ResponseEntity<Object> handlePropertyReference(
      PropertyReferenceException ex, WebRequest request) {
    // Creamos un ProblemDetail con el status y el mensaje de la excepcion.
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                ? ex.getMessage()
                : "The requested property for sorting or searching does not exist on the resource.");
    // Establecemos el título del error.
    problemDetail.setTitle("PropertyReferenceException: Invalid Property Reference");
    // Añadir una propiedad al cuerpo para mayor claridad.
    problemDetail.setProperty("invalidProperty", ex.getPropertyName());
    // Adjuntamos un enlace a la documentación del error.
    problemDetail.setType(
        URI.create(
            "https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/mapping/PropertyReferenceException.html"));
    // Registramos el error en el log.
    log.warn("Property Reference Exception (400 Bad Request): {}", ex);
    // Devolvemos la respuesta con el ProblemDetail y el estado BAD_REQUEST.
    return handleExceptionInternal(
        ex, problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }
}
