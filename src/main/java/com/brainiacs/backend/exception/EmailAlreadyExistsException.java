package com.brainiacs.backend.exception;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException() {
    super("errors.user.emailAlreadyExists");
  }
}
