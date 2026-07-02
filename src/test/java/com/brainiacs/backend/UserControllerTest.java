package com.brainiacs.backend;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.brainiacs.backend.exception.AvatarNotFoundException;
import com.brainiacs.backend.exception.UserNotFoundException;
import com.brainiacs.backend.user.UserController;
import com.brainiacs.backend.user.UserDto;
import com.brainiacs.backend.user.UserPatchDto;
import com.brainiacs.backend.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = UserController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      UserDetailsServiceAutoConfiguration.class
    })
@WithMockUser
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  // ───── GET /api/users ─────

  @Test
  void getAll_shouldReturn200WithEmptyPage() throws Exception {
    Page<UserDto> emptyPage = new PageImpl<>(List.of());
    when(userService.getAllUsers(1, 6)).thenReturn(emptyPage);

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void getAll_shouldReturn200WithUsers() throws Exception {
    UserDto dto = new UserDto(1L, "Jan", "Kowalski", "jan@test.com", null);
    Page<UserDto> page = new PageImpl<>(List.of(dto));
    when(userService.getAllUsers(1, 6)).thenReturn(page);

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].firstName").value("Jan"))
        .andExpect(jsonPath("$.content[0].lastName").value("Kowalski"))
        .andExpect(jsonPath("$.content[0].email").value("jan@test.com"));
  }

  @Test
  void getAll_shouldReturn400_whenPageIsLessThanOne() throws Exception {
    mockMvc.perform(get("/api/users").param("page", "0")).andExpect(status().isBadRequest());
  }

  @Test
  void getAll_shouldReturn400_whenSizeExceedsMax() throws Exception {
    mockMvc.perform(get("/api/users").param("size", "101")).andExpect(status().isBadRequest());
  }

  // ───── GET /api/users/{id} ─────

  @Test
  void getById_shouldReturn200_whenUserExists() throws Exception {
    UserDto dto = new UserDto(1L, "Jan", "Kowalski", "jan@test.com", null);
    when(userService.getUserById(1L)).thenReturn(dto);

    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("Jan"))
        .andExpect(jsonPath("$.email").value("jan@test.com"));
  }

  @Test
  void getById_shouldReturn404_whenUserNotFound() throws Exception {
    when(userService.getUserById(99L)).thenThrow(new UserNotFoundException());

    mockMvc.perform(get("/api/users/99")).andExpect(status().isNotFound());
  }

  // ───── GET /api/users/{id}/avatar ─────

  @Test
  void getAvatar_shouldReturn200_whenAvatarExists() throws Exception {
    UserService.AvatarData avatarData =
        new UserService.AvatarData("fake-image".getBytes(), "image/png");
    when(userService.getAvatar(1L)).thenReturn(avatarData);

    mockMvc
        .perform(get("/api/users/1/avatar"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG));
  }

  @Test
  void getAvatar_shouldReturn404_whenUserNotFound() throws Exception {
    when(userService.getAvatar(99L)).thenThrow(new UserNotFoundException());

    mockMvc.perform(get("/api/users/99/avatar")).andExpect(status().isNotFound());
  }

  @Test
  void getAvatar_shouldReturn404_whenUserHasNoAvatar() throws Exception {
    when(userService.getAvatar(1L)).thenThrow(new AvatarNotFoundException());

    mockMvc.perform(get("/api/users/1/avatar")).andExpect(status().isNotFound());
  }

  // ───── POST /api/users ─────

  @Test
  void create_shouldReturn201WithCreatedUser() throws Exception {
    UserDto request = new UserDto(null, "Anna", "Nowak", "anna@test.com", null);
    UserDto response = new UserDto(1L, "Anna", "Nowak", "anna@test.com", null);
    when(userService.createUser(any(UserDto.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("Anna"))
        .andExpect(jsonPath("$.email").value("anna@test.com"));
  }

  @Test
  void create_shouldReturn409_whenEmailAlreadyExists() throws Exception {
    UserDto request = new UserDto(null, "Anna", "Nowak", "anna@test.com", null);
    when(userService.createUser(any(UserDto.class)))
        .thenThrow(new com.brainiacs.backend.exception.EmailAlreadyExistsException());

    mockMvc
        .perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.email").exists());
  }

  @Test
  void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
    UserDto request = new UserDto(null, "Anna", "Nowak", "invalid-email", null);

    mockMvc
        .perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.email").exists());
  }

  @Test
  void create_shouldReturn400_whenFirstNameIsBlank() throws Exception {
    UserDto request = new UserDto(null, "", "Nowak", "anna@test.com", null);

    mockMvc
        .perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.firstName").exists());
  }

  @Test
  void create_shouldReturn400_whenLastNameIsBlank() throws Exception {
    UserDto request = new UserDto(null, "Anna", "", "anna@test.com", null);

    mockMvc
        .perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.lastName").exists());
  }

  @Test
  void create_shouldReturn400_whenFirstNameIsTooShort() throws Exception {
    UserDto request = new UserDto(null, "A", "Nowak", "anna@test.com", null);

    mockMvc
        .perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.firstName").exists());
  }

  // ───── PATCH /api/users/{id} ─────

  @Test
  void update_shouldReturn200WithUpdatedUser() throws Exception {
    UserPatchDto request = new UserPatchDto("Janek", "Nowak", "janek@test.com");
    UserDto response = new UserDto(1L, "Janek", "Nowak", "janek@test.com", null);
    when(userService.updateUser(eq(1L), any(UserPatchDto.class))).thenReturn(response);

    mockMvc
        .perform(
            patch("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Janek"))
        .andExpect(jsonPath("$.email").value("janek@test.com"));
  }

  @Test
  void update_shouldReturn400_whenEmailIsInvalid() throws Exception {
    UserPatchDto request = new UserPatchDto("Anna", "Nowak", "invalid-email");

    mockMvc
        .perform(
            patch("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.email").exists());
  }

  @Test
  void update_shouldReturn400_whenFirstNameIsTooShort() throws Exception {
    UserPatchDto request = new UserPatchDto("A", null, null);

    mockMvc
        .perform(
            patch("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.firstName").exists());
  }

  @Test
  void update_shouldReturn404_whenUserNotFound() throws Exception {
    UserPatchDto request = new UserPatchDto("Janek", "Nowak", "janek@test.com");
    when(userService.updateUser(eq(99L), any(UserPatchDto.class)))
        .thenThrow(new UserNotFoundException());

    mockMvc
        .perform(
            patch("/api/users/99")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void update_shouldReturn409_whenEmailAlreadyTaken() throws Exception {
    UserPatchDto request = new UserPatchDto(null, null, "taken@test.com");
    when(userService.updateUser(eq(1L), any(UserPatchDto.class)))
        .thenThrow(new com.brainiacs.backend.exception.EmailAlreadyExistsException());

    mockMvc
        .perform(
            patch("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.email").exists());
  }

  // ───── DELETE /api/users/{id} ─────

  @Test
  void delete_shouldReturn204() throws Exception {
    doNothing().when(userService).deleteUser(1L);

    mockMvc.perform(delete("/api/users/1").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  void delete_shouldReturn404_whenUserNotFound() throws Exception {
    doThrow(new UserNotFoundException()).when(userService).deleteUser(99L);

    mockMvc.perform(delete("/api/users/99").with(csrf())).andExpect(status().isNotFound());
  }

  // ───── PUT /api/users/{id}/avatar ─────

  @Test
  void uploadAvatar_shouldReturn200_whenImageFile() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.png", "image/png", "fake-image".getBytes());
    UserDto userDto = new UserDto(1L, "Jan", "Kowalski", "jan@example.com", "/api/users/1/avatar");
    when(userService.updateAvatar(eq(1L), any(), eq("image/png"))).thenReturn(userDto);

    mockMvc
        .perform(multipart(HttpMethod.PUT, "/api/users/1/avatar").with(csrf()).file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.avatar").value("/api/users/1/avatar"));
  }

  @Test
  void uploadAvatar_shouldReturn200_whenJpegFile() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "fake-image".getBytes());
    UserDto userDto = new UserDto(1L, "Jan", "Kowalski", "jan@example.com", "/api/users/1/avatar");
    when(userService.updateAvatar(eq(1L), any(), eq("image/jpeg"))).thenReturn(userDto);

    mockMvc
        .perform(multipart(HttpMethod.PUT, "/api/users/1/avatar").with(csrf()).file(file))
        .andExpect(status().isOk());
  }

  @Test
  void uploadAvatar_shouldReturn400_whenNotImage() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "document.pdf", "application/pdf", "fake-pdf".getBytes());

    mockMvc
        .perform(multipart(HttpMethod.PUT, "/api/users/1/avatar").with(csrf()).file(file))
        .andExpect(status().isBadRequest());
  }

  @Test
  void uploadAvatar_shouldReturn400_whenFileIsInvalidImage() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.png", "image/png", "not-really-an-image".getBytes());
    when(userService.updateAvatar(eq(1L), any(), eq("image/png")))
        .thenThrow(new com.brainiacs.backend.exception.InvalidImageException());

    mockMvc
        .perform(multipart(HttpMethod.PUT, "/api/users/1/avatar").with(csrf()).file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.file").exists());
  }

  @Test
  void uploadAvatar_shouldReturn404_whenUserNotFound() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.png", "image/png", "fake-image".getBytes());
    when(userService.updateAvatar(eq(99L), any(), eq("image/png")))
        .thenThrow(new UserNotFoundException());

    mockMvc
        .perform(multipart(HttpMethod.PUT, "/api/users/99/avatar").with(csrf()).file(file))
        .andExpect(status().isNotFound());
  }
}
