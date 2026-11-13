package org.vedruna.futurjob.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.vedruna.futurjob.controller.dto.UserDto;
import org.vedruna.futurjob.controller.dto.authdto.AuthResponseDto;
import org.vedruna.futurjob.controller.dto.authdto.LoginRequestDto;
import org.vedruna.futurjob.controller.dto.authdto.RefreshRequestDto;
import org.vedruna.futurjob.controller.dto.authdto.RegisterRequestDto;
import org.vedruna.futurjob.persistance.model.UserEntity;

@Tag(name = "Auth", description = "Authorization endpoints")
@Validated
@RequestMapping("/api/v1/auth")
public interface AuthController {

  @PostMapping(value = "/register")
  ResponseEntity<UserDto> register(@RequestBody @Valid RegisterRequestDto request);

  @PostMapping(value = "/login")
  public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto request);

  @GetMapping(value = "/me")
  public UserDto me(@AuthenticationPrincipal UserEntity userLogueado);

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponseDto> refreshToken(
      @RequestBody @Valid RefreshRequestDto request);
}
