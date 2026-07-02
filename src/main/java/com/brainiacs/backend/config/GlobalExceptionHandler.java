package com.brainiacs.backend.config;

import com.brainiacs.backend.exception.AvatarNotFoundException;
import com.brainiacs.backend.exception.EmailAlreadyExistsException;
import com.brainiacs.backend.exception.InvalidImageException;
import com.brainiacs.backend.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  public GlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(
      EmailAlreadyExistsException ex, Locale locale) {
    String message = messageSource.getMessage(ex.getMessage(), null, locale);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("email", message));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFound(
      UserNotFoundException ex, Locale locale) {
    String message = messageSource.getMessage(ex.getMessage(), null, locale);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("non_field_errors", message));
  }

  @ExceptionHandler(AvatarNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleAvatarNotFound(
      AvatarNotFoundException ex, Locale locale) {
    String message = messageSource.getMessage(ex.getMessage(), null, locale);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("non_field_errors", message));
  }

  @ExceptionHandler(InvalidImageException.class)
  public ResponseEntity<Map<String, String>> handleInvalidImage(
      InvalidImageException ex, Locale locale) {
    String message = messageSource.getMessage(ex.getMessage(), null, locale);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("file", message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolation(
      ConstraintViolationException ex) {
    Map<String, String> errors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    v -> v.getMessage(),
                    (existing, replacement) -> existing));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntimeException(
      RuntimeException ex, Locale locale) {
    log.error("Unexpected error occurred", ex);
    String message = messageSource.getMessage("errors.general.unexpected", null, locale);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("non_field_errors", message));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    error ->
                        error.getDefaultMessage() != null
                            ? error.getDefaultMessage()
                            : "Invalid value",
                    (existing, replacement) -> existing));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }
}
