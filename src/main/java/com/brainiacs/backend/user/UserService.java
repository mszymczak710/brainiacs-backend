package com.brainiacs.backend.user;

import com.brainiacs.backend.exception.AvatarNotFoundException;
import com.brainiacs.backend.exception.EmailAlreadyExistsException;
import com.brainiacs.backend.exception.InvalidImageException;
import com.brainiacs.backend.exception.UserNotFoundException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final ImageResizer imageResizer;
  private final UserMapper userMapper;

  public record AvatarData(byte[] bytes, String contentType) {}

  public AvatarData getAvatar(Long id) {
    User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    if (user.getAvatar() == null) {
      throw new AvatarNotFoundException();
    }
    return new AvatarData(user.getAvatar(), user.getAvatarContentType());
  }

  public Page<UserDto> getAllUsers(int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return userRepository.findAll(pageable).map(userMapper::toDto);
  }

  public UserDto getUserById(Long id) {
    User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    return userMapper.toDto(user);
  }

  public UserDto createUser(UserDto dto) {
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new EmailAlreadyExistsException();
    }
    User user = new User(null, dto.getFirstName(), dto.getLastName(), dto.getEmail(), null, null);
    return userMapper.toDto(userRepository.save(user));
  }

  public UserDto updateUser(Long id, UserDto dto) {
    User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

    if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
      throw new EmailAlreadyExistsException();
    }

    user.setFirstName(dto.getFirstName());
    user.setLastName(dto.getLastName());
    user.setEmail(dto.getEmail());

    return userMapper.toDto(userRepository.save(user));
  }

  public UserDto updateAvatar(Long id, byte[] bytes, String contentType) {
    User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

    byte[] resized;
    try {
      resized = imageResizer.resize(bytes, contentType);
    } catch (IOException e) {
      throw new InvalidImageException();
    }

    user.setAvatar(resized);
    user.setAvatarContentType(contentType);
    return userMapper.toDto(userRepository.save(user));
  }

  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException();
    }
    userRepository.deleteById(id);
  }
}
