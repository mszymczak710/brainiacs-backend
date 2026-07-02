package com.brainiacs.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchDto {

  @Size(min = 2, max = 50, message = "{errors.user.firstName.size}")
  private String firstName;

  @Size(min = 2, max = 50, message = "{errors.user.lastName.size}")
  private String lastName;

  @Email(message = "{errors.user.email.invalid}")
  private String email;
}
