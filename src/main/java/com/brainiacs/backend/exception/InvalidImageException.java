package com.brainiacs.backend.exception;

public class InvalidImageException extends RuntimeException {
  public InvalidImageException() {
    super("errors.user.invalidImage");
  }
}
