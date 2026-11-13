package org.vedruna.futurjob.controller.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.vedruna.futurjob.controller.dto.MainDto;

class MainControllerImplTest {

  private MainControllerImpl mainController;

  @BeforeEach
  void setUp() {
    mainController = new MainControllerImpl();
  }

  @Test
  void testIndex() {
    ResponseEntity<MainDto> response = mainController.index();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Hello world desde endpoint publico", response.getBody().getMessage());
  }

  @Test
  void testPrivateIndex() {
    ResponseEntity<MainDto> response = mainController.privateIndex();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Hello world desde endpoint private", response.getBody().getMessage());
  }

  @Test
  void testAdminIndex() {
    ResponseEntity<MainDto> response = mainController.adminIndex();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(
        "Hello world desde endpoint protegido para admin", response.getBody().getMessage());
  }
}
