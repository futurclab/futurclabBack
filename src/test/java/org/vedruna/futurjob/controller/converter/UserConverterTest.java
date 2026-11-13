package org.vedruna.futurjob.controller.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vedruna.futurjob.controller.dto.UserDto;
import org.vedruna.futurjob.controller.dto.authdto.LoginRequestDto;
import org.vedruna.futurjob.controller.dto.authdto.RegisterRequestDto;
import org.vedruna.futurjob.persistance.model.RolEntity;
import org.vedruna.futurjob.persistance.model.UserEntity;

class UserConverterTest {

  private UserConverter userConverter;

  @BeforeEach
  void setUp() {
    userConverter = new UserConverterImpl();
  }

  @Test
  void testToDto_withValidUserAndRole() {
    RolEntity role = new RolEntity();
    role.setUserId(2);
    role.setRolName("ROLE_USER");

    LocalDateTime now = LocalDateTime.now();

    UserEntity user = new UserEntity();
    user.setUserId(1);
    user.setUsername("john");
    user.setEmail("john@example.com");
    user.setPassword("secret");
    user.setCreateDate(now);
    user.setAccountExpirationDate(now.plusDays(30));
    user.setIsLocked(false);
    user.setCredentialsExpirationDate(now.plusDays(15));
    user.setIsEnabled(true);
    user.setUserRol(role);

    UserDto dto = userConverter.toDto(user);

    assertNotNull(dto);
    assertEquals(1, dto.getUserId());
    assertEquals("john", dto.getUsername());
    assertEquals("john@example.com", dto.getEmail());
    assertEquals(now, dto.getCreateDate());
    assertEquals(now.plusDays(30), dto.getAccountExpirationDate());
    assertEquals(false, dto.getIsLocked());
    assertEquals(now.plusDays(15), dto.getCredentialsExpirationDate());
    assertEquals(true, dto.getIsEnabled());
    assertEquals("ROLE_USER", dto.getUserRolName());
  }

  @Test
  void testToDto_withUserAndNullRole() {
    UserEntity user = new UserEntity();
    user.setUserId(2);
    user.setUsername("jane");
    user.setUserRol(null);

    UserDto dto = userConverter.toDto(user);

    assertNotNull(dto);
    assertEquals(2, dto.getUserId());
    assertEquals("jane", dto.getUsername());
    assertNull(dto.getUserRolName());
  }

  @Test
  void testToDto_withNullUser() {
    assertNull(userConverter.toDto(null));
  }

  @Test
  void testLoginToEntity_withValidDto() {
    LoginRequestDto request = new LoginRequestDto();
    request.setUsername("john");
    request.setPassword("password123");

    UserEntity entity = userConverter.loginToEntity(request);

    assertNotNull(entity);
    assertEquals("john", entity.getUsername());
    assertEquals("password123", entity.getPassword());
  }

  @Test
  void testLoginToEntity_withNullDto() {
    assertNull(userConverter.loginToEntity(null));
  }

  @Test
  void testRegisterToEntity_withValidDto() {
    RegisterRequestDto request = new RegisterRequestDto();
    request.setUsername("alice");
    request.setEmail("alice@example.com");
    request.setPassword("securePass");

    UserEntity entity = userConverter.registerToEntity(request);

    assertNotNull(entity);
    assertEquals("alice", entity.getUsername());
    assertEquals("alice@example.com", entity.getEmail());
    assertEquals("securePass", entity.getPassword());
  }

  @Test
  void testRegisterToEntity_withNullDto() {
    assertNull(userConverter.registerToEntity(null));
  }
}
