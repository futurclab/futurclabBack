package org.vedruna.futurjob.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.vedruna.futurjob.controller.MainController;
import org.vedruna.futurjob.controller.dto.MainDto;

@CrossOrigin
@RestController
public class MainControllerImpl implements MainController {

  public ResponseEntity<MainDto> index() {
    return ResponseEntity.ok(new MainDto("Hello world desde endpoint publico"));
  }

  public ResponseEntity<MainDto> privateIndex() {
    return ResponseEntity.ok(new MainDto("Hello world desde endpoint private"));
  }

  public ResponseEntity<MainDto> adminIndex() {
    return ResponseEntity.ok(new MainDto("Hello world desde endpoint protegido para admin"));
  }
}
