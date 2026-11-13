package org.vedruna.futurjob.controller.dto.authdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

  @JsonProperty("token_type")
  final String tokenType = "Bearer";

  @JsonProperty("access_token")
  String accessToken;

  @JsonProperty("expires_in")
  Long expiresIn;

  @JsonProperty("refresh_token")
  String refreshToken;

  @JsonProperty("scope")
  String scope;
}
