package org.vedruna.futurjob.controller.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserDto {

  Integer userId;

  String username;

  String email;

  LocalDateTime createDate;

  LocalDateTime accountExpirationDate;

  Boolean isLocked;

  LocalDateTime credentialsExpirationDate;

  Boolean isEnabled;

  String userRolName;
}
