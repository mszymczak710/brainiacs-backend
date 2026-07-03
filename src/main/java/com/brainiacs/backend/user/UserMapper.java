package com.brainiacs.backend.user;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class UserMapper {

  public UserDto toDto(User u) {
    String avatarUrl =
        u.getAvatar() != null
            ? ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/{id}/avatar")
                .buildAndExpand(u.getId())
                .toUriString()
            : null;
    return new UserDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), avatarUrl);
  }
}
