package org.vedruna.futurjob.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.vedruna.futurjob.controller.dto.MainDto;

@Tag(name = "Main", description = "Test endpoints")
@Validated
@RequestMapping("/api/v1/main")
public interface MainController {

  @GetMapping("/public")
  public ResponseEntity<MainDto> index();

  @GetMapping("/private")
  public ResponseEntity<MainDto> privateIndex();

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/admin")
  public ResponseEntity<MainDto> adminIndex();
}
