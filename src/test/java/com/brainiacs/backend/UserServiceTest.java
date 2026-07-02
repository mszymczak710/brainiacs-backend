package com.brainiacs.backend;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brainiacs.backend.exception.AvatarNotFoundException;
import com.brainiacs.backend.exception.EmailAlreadyExistsException;
import com.brainiacs.backend.exception.InvalidImageException;
import com.brainiacs.backend.exception.UserNotFoundException;
import com.brainiacs.backend.user.ImageResizer;
import com.brainiacs.backend.user.User;
import com.brainiacs.backend.user.UserDto;
import com.brainiacs.backend.user.UserPatchDto;
import com.brainiacs.backend.user.UserRepository;
import com.brainiacs.backend.user.UserService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private ImageResizer imageResizer;

  @InjectMocks private UserService userService;

  // ───── getAllUsers ─────

  @Test
  void getAllUsers_shouldReturnEmptyPage_whenNoUsers() {
    Page<User> emptyPage = new PageImpl<>(List.of());
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

    Page<UserDto> result = userService.getAllUsers(1, 6);

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void getAllUsers_shouldReturnMappedDtos() {
    byte[] avatar = "fake-image".getBytes();
    User user = new User(1L, "Jan", "Kowalski", "jan@test.com", avatar, "image/png");
    Page<User> page = new PageImpl<>(List.of(user));
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

    Page<UserDto> result = userService.getAllUsers(1, 6);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Jan");
    assertThat(result.getContent().get(0).getAvatar()).isEqualTo("/api/users/1/avatar");
  }

  @Test
  void getAllUsers_shouldReturnNullAvatar_whenAvatarIsNull() {
    User user = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    Page<User> page = new PageImpl<>(List.of(user));
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

    Page<UserDto> result = userService.getAllUsers(1, 6);

    assertThat(result.getContent().get(0).getAvatar()).isNull();
  }

  // ───── getUserById ─────

  @Test
  void getUserById_shouldReturnDto_whenUserExists() {
    User user = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserDto result = userService.getUserById(1L);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getFirstName()).isEqualTo("Jan");
    assertThat(result.getEmail()).isEqualTo("jan@test.com");
  }

  @Test
  void getUserById_shouldThrow_whenUserNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserById(99L))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ───── getAvatar ─────

  @Test
  void getAvatar_shouldReturnAvatarData_whenAvatarExists() {
    byte[] avatar = "fake-image".getBytes();
    User user = new User(1L, "Jan", "Kowalski", "jan@test.com", avatar, "image/png");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserService.AvatarData result = userService.getAvatar(1L);

    assertThat(result.bytes()).isEqualTo(avatar);
    assertThat(result.contentType()).isEqualTo("image/png");
  }

  @Test
  void getAvatar_shouldThrowUserNotFound_whenUserDoesNotExist() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getAvatar(99L)).isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void getAvatar_shouldThrowAvatarNotFound_whenUserHasNoAvatar() {
    User user = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> userService.getAvatar(1L)).isInstanceOf(AvatarNotFoundException.class);
  }

  // ───── createUser ─────

  @Test
  void createUser_shouldSaveAndReturnDto() {
    UserDto dto = new UserDto(null, "Anna", "Nowak", "anna@test.com", null);
    User saved = new User(1L, "Anna", "Nowak", "anna@test.com", "img".getBytes(), "image/png");
    when(userRepository.save(any(User.class))).thenReturn(saved);

    UserDto result = userService.createUser(dto);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getFirstName()).isEqualTo("Anna");
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void createUser_shouldHandleNullAvatar() {
    UserDto dto = new UserDto(null, "Anna", "Nowak", "anna@test.com", null);
    User saved = new User(1L, "Anna", "Nowak", "anna@test.com", null, null);
    when(userRepository.save(any(User.class))).thenReturn(saved);

    UserDto result = userService.createUser(dto);

    assertThat(result.getAvatar()).isNull();
  }

  @Test
  void createUser_shouldThrow_whenEmailAlreadyExists() {
    UserDto dto = new UserDto(null, "Anna", "Nowak", "anna@test.com", null);
    when(userRepository.existsByEmail("anna@test.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.createUser(dto))
        .isInstanceOf(EmailAlreadyExistsException.class);

    verify(userRepository, never()).save(any(User.class));
  }

  // ───── updateUser ─────

  @Test
  void updateUser_shouldUpdateFieldsAndReturnDto() {
    User existing = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    UserPatchDto dto = new UserPatchDto("Janek", "Nowak", "janek@test.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.save(any(User.class))).thenReturn(existing);

    UserDto result = userService.updateUser(1L, dto);

    assertThat(result.getFirstName()).isEqualTo("Janek");
    assertThat(result.getLastName()).isEqualTo("Nowak");
    assertThat(result.getEmail()).isEqualTo("janek@test.com");
    verify(userRepository, times(1)).save(existing);
  }

  @Test
  void updateUser_shouldThrow_whenUserNotFound() {
    UserPatchDto dto = new UserPatchDto("Janek", "Nowak", "janek@test.com");
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.updateUser(99L, dto))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void updateUser_shouldThrow_whenEmailAlreadyTakenByAnotherUser() {
    User existing = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    UserPatchDto dto = new UserPatchDto(null, null, "taken@test.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.updateUser(1L, dto))
        .isInstanceOf(EmailAlreadyExistsException.class);

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void updateUser_shouldIgnoreBlankEmail() {
    User existing = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    UserPatchDto dto = new UserPatchDto("Janek", null, "");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.save(any(User.class))).thenReturn(existing);

    UserDto result = userService.updateUser(1L, dto);

    assertThat(result.getEmail()).isEqualTo("jan@test.com");
    assertThat(result.getFirstName()).isEqualTo("Janek");
  }

  @Test
  void updateUser_shouldNotCheckUniqueness_whenEmailUnchanged() {
    User existing = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    UserPatchDto dto = new UserPatchDto(null, null, "jan@test.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.save(any(User.class))).thenReturn(existing);

    userService.updateUser(1L, dto);

    verify(userRepository, never()).existsByEmail(any());
  }

  // ───── updateAvatar ─────

  @Test
  void updateAvatar_shouldResizeSaveAvatarBytesAndReturnDto() throws IOException {
    User existing = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    byte[] rawAvatar = "raw-image".getBytes();
    byte[] resizedAvatar = "resized-image".getBytes();
    User saved = new User(1L, "Jan", "Kowalski", "jan@test.com", resizedAvatar, "image/png");

    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(imageResizer.resize(rawAvatar, "image/png")).thenReturn(resizedAvatar);
    when(userRepository.save(existing)).thenReturn(saved);

    UserDto result = userService.updateAvatar(1L, rawAvatar, "image/png");

    assertThat(existing.getAvatar()).isEqualTo(resizedAvatar);
    assertThat(existing.getAvatarContentType()).isEqualTo("image/png");
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getAvatar()).isEqualTo("/api/users/1/avatar");
    verify(userRepository, times(1)).save(existing);
  }

  @Test
  void updateAvatar_shouldThrowUserNotFound_whenUserDoesNotExist() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.updateAvatar(99L, new byte[] {}, "image/png"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void updateAvatar_shouldThrowInvalidImage_whenResizeFails() throws IOException {
    User existing = new User(1L, "Jan", "Kowalski", "jan@test.com", null, null);
    byte[] corrupted = "not-an-image".getBytes();

    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(imageResizer.resize(corrupted, "image/png")).thenThrow(new IOException("corrupted"));

    assertThatThrownBy(() -> userService.updateAvatar(1L, corrupted, "image/png"))
        .isInstanceOf(InvalidImageException.class);

    verify(userRepository, never()).save(any(User.class));
  }

  // ───── deleteUser ─────

  @Test
  void deleteUser_shouldCallRepositoryDeleteById_whenUserExists() {
    when(userRepository.existsById(1L)).thenReturn(true);

    userService.deleteUser(1L);

    verify(userRepository, times(1)).deleteById(1L);
  }

  @Test
  void deleteUser_shouldThrow_whenUserNotFound() {
    when(userRepository.existsById(99L)).thenReturn(false);

    assertThatThrownBy(() -> userService.deleteUser(99L)).isInstanceOf(UserNotFoundException.class);

    verify(userRepository, never()).deleteById(anyLong());
  }
}
