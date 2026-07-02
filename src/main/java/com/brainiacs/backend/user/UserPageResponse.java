package com.brainiacs.backend.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
public class UserPageResponse {
  private List<UserDto> content;
  private int pageSize;
  private int currentPage;
  private int totalPages;
  private long totalElements;

  public static UserPageResponse from(Page<UserDto> page) {
    return new UserPageResponse(
        page.getContent(),
        page.getSize(),
        page.getNumber() + 1,
        page.getTotalPages(),
        page.getTotalElements());
  }
}
