package com.brainiacs.backend.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;

  @GetMapping
  public UserPageResponse getAll(
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "6") @Min(1) @Max(100) int size) {
    return UserPageResponse.from(userService.getAllUsers(page, size));
  }

  @GetMapping("/{id}")
  public UserDto getById(@PathVariable Long id) {
    return userService.getUserById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto create(@Valid @RequestBody UserDto dto) {
    return userService.createUser(dto);
  }

  @GetMapping("/{id}/avatar")
  public ResponseEntity<byte[]> getAvatar(@PathVariable Long id) {
    UserService.AvatarData avatar = userService.getAvatar(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(avatar.contentType()))
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
        .body(avatar.bytes());
  }

  @PutMapping("/{id}/avatar")
  public ResponseEntity<UserDto> uploadAvatar(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {

    if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
      return ResponseEntity.badRequest().build();
    }

    UserDto updatedUser = userService.updateAvatar(id, file.getBytes(), file.getContentType());
    return ResponseEntity.ok(updatedUser);
  }

  @PutMapping("/{id}")
  public UserDto update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
    return userService.updateUser(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    userService.deleteUser(id);
  }
}
