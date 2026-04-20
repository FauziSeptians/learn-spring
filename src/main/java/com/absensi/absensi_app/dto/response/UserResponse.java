package com.absensi.absensi_app.dto.response;

import com.absensi.absensi_app.enums.Role;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private Role role;
  private LocalDateTime createdAt;
}
