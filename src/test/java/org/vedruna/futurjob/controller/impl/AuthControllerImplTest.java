package org.vedruna.futurjob.controller.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.vedruna.futurjob.controller.converter.UserConverter;
import org.vedruna.futurjob.controller.dto.UserDto;
import org.vedruna.futurjob.controller.dto.authdto.AuthResponseDto;
import org.vedruna.futurjob.controller.dto.authdto.LoginRequestDto;
import org.vedruna.futurjob.controller.dto.authdto.RefreshRequestDto;
import org.vedruna.futurjob.controller.dto.authdto.RegisterRequestDto;
import org.vedruna.futurjob.persistance.model.UserEntity;
import org.vedruna.futurjob.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerImplTest {

  @Mock private AuthService authService;

  @Mock private UserConverter userConverter;

  @InjectMocks private AuthControllerImpl authController;

  @Test
  void testRegister() {
    RegisterRequestDto request = new RegisterRequestDto();
    request.setUsername("testuser");
    request.setPassword("password123");
    request.setEmail("test@example.com");

    UserEntity unconvertedEntity = new UserEntity();
    unconvertedEntity.setUsername("testuser");

    UserEntity savedEntity = new UserEntity();
    savedEntity.setUserId(1);
    savedEntity.setUsername("testuser");

    UserDto expectedDto = new UserDto();
    expectedDto.setUserId(1);
    expectedDto.setUsername("testuser");

    when(userConverter.registerToEntity(request)).thenReturn(unconvertedEntity);
    when(authService.register(unconvertedEntity)).thenReturn(savedEntity);
    when(userConverter.toDto(savedEntity)).thenReturn(expectedDto);

    ResponseEntity<UserDto> response = authController.register(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expectedDto, response.getBody());
    verify(userConverter).registerToEntity(request);
    verify(authService).register(unconvertedEntity);
    verify(userConverter).toDto(savedEntity);
  }

  @Test
  void testLogin() {
    LoginRequestDto request = new LoginRequestDto();
    request.setUsername("testuser");
    request.setPassword("password123");

    UserEntity loginEntity = new UserEntity();
    loginEntity.setUsername("testuser");
    loginEntity.setPassword("password123");

    AuthResponseDto expectedAuthResponse =
        new AuthResponseDto("access-token-123", 3600L, "refresh-token-123", null);

    when(userConverter.loginToEntity(request)).thenReturn(loginEntity);
    when(authService.login(loginEntity)).thenReturn(expectedAuthResponse);

    ResponseEntity<AuthResponseDto> response = authController.login(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("access-token-123", response.getBody().getAccessToken());
    assertEquals(expectedAuthResponse, response.getBody());
    verify(userConverter).loginToEntity(request);
    verify(authService).login(loginEntity);
  }

  @Test
  void testMe() {
    UserEntity userLogueado = new UserEntity();
    userLogueado.setUserId(10);
    userLogueado.setUsername("loggeduser");

    UserDto expectedDto = new UserDto();
    expectedDto.setUserId(10);
    expectedDto.setUsername("loggeduser");

    when(userConverter.toDto(userLogueado)).thenReturn(expectedDto);

    UserDto result = authController.me(userLogueado);

    assertNotNull(result);
    assertEquals(expectedDto, result);
    verify(userConverter).toDto(userLogueado);
  }

  @Test
  void testRefreshToken() {
    RefreshRequestDto request = new RefreshRequestDto();
    request.setRefreshToken("sample-refresh-token");

    AuthResponseDto expectedAuthResponse =
        new AuthResponseDto("new-access-token", 3600L, "sample-refresh-token", null);

    when(authService.refreshToken("sample-refresh-token")).thenReturn(expectedAuthResponse);

    ResponseEntity<AuthResponseDto> response = authController.refreshToken(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("new-access-token", response.getBody().getAccessToken());
    assertEquals("sample-refresh-token", response.getBody().getRefreshToken());
    verify(authService).refreshToken("sample-refresh-token");
  }
}
