package com.brainiacs.backend.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super("errors.user.notFound");
  }
}
