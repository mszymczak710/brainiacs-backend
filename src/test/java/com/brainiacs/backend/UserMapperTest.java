package com.brainiacs.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.brainiacs.backend.user.User;
import com.brainiacs.backend.user.UserDto;
import com.brainiacs.backend.user.UserMapper;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private final UserMapper mapper = new UserMapper();

  @Test
  void shouldBuildAvatarUrlWhenAvatarPresent() {
    User user =
        new User(1L, "Jan", "Kowalski", "jan@example.com", new byte[] {1, 2, 3}, "image/png");

    UserDto dto = mapper.toDto(user);

    assertThat(dto.getAvatar()).isEqualTo("/api/users/1/avatar");
  }

  @Test
  void shouldReturnNullAvatarUrlWhenAvatarAbsent() {
    User user = new User(1L, "Jan", "Kowalski", "jan@example.com", null, null);

    UserDto dto = mapper.toDto(user);

    assertThat(dto.getAvatar()).isNull();
  }

  @Test
  void shouldMapAllSimpleFields() {
    User user = new User(1L, "Jan", "Kowalski", "jan@example.com", null, null);

    UserDto dto = mapper.toDto(user);

    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getFirstName()).isEqualTo("Jan");
    assertThat(dto.getLastName()).isEqualTo("Kowalski");
    assertThat(dto.getEmail()).isEqualTo("jan@example.com");
  }
}
