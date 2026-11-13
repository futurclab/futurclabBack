package org.vedruna.futurjob.persistance.exception;

import java.util.NoSuchElementException;

/**
 * Excepción de tiempo de ejecución personalizada utilizada para indicar que un usuario específico
 * no ha sido encontrado en la base de datos o en el repositorio.
 *
 * <p>Esta clase extiende {@link NoSuchElementException}, que es la excepción estándar de Java
 * utilizada para indicar que no se encontró un elemento al intentar acceder a él. Al ser una {@code
 * RuntimeException}, no requiere ser declarada en la firma de los métodos que la lanzan.
 */
public class UserNotFoundException extends NoSuchElementException {

  /**
   * Constructor por defecto que llama al constructor de la clase padre ({@link
   * NoSuchElementException}) con un mensaje predeterminado.
   */
  public UserNotFoundException() {
    super("User not found");
  }
}
