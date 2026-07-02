package com.brainiacs.backend.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Long id;

  @NotBlank(message = "{errors.user.firstName.required}")
  @Size(min = 2, max = 50, message = "{errors.user.firstName.size}")
  private String firstName;

  @NotBlank(message = "{errors.user.lastName.required}")
  @Size(min = 2, max = 50, message = "{errors.user.lastName.size}")
  private String lastName;

  @NotBlank(message = "{errors.user.email.required}")
  @Email(message = "{errors.user.email.invalid}")
  private String email;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String avatar;
}
