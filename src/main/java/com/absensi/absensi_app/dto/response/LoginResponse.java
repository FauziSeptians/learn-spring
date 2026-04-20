package com.absensi.absensi_app.dto.response;

import com.absensi.absensi_app.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
  private String token;
  private String email;
  private String name;
  private Role role;
}
