package com.brainiacs.backend.exception;

public class AvatarNotFoundException extends RuntimeException {
  public AvatarNotFoundException() {
    super("errors.user.avatarNotFound");
  }
}
