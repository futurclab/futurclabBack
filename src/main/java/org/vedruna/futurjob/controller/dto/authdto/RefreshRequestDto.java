package org.vedruna.futurjob.controller.dto.authdto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequestDto {
  /** El Refresh Token que se desea canjear por un nuevo Access Token. */
  @NotBlank String refreshToken;
}
