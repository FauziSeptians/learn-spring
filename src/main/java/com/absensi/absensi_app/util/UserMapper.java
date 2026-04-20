package com.absensi.absensi_app.util;

import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .build();
  }

  public List<UserResponse> toResponseList(List<User> users) {
    return users.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
