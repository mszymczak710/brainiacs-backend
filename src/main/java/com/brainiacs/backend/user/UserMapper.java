package com.brainiacs.backend.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto toDto(User u) {
    String avatarUrl = u.getAvatar() != null ? "/api/users/" + u.getId() + "/avatar" : null;
    return new UserDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), avatarUrl);
  }
}
