package com.absensi.absensi_app.controller;

import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.services.impl.UserServiceImpl;
import com.absensi.absensi_app.util.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "API untuk manajemen user")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping
    @Operation(summary = "Get semua user", description = "Mengambil semua data user")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {

        List<UserResponse> users = userService.findAll();

        return ResponseEntity.ok(ApiMapper.success("sukses mengambil semua data user", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Mengambil data user berdasarkan ID")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable Long id) {

        UserResponse user = userService.findById(id);

        return ResponseEntity.ok(ApiMapper.success("sukses mengambil data", user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update data user berdasarkan ID")
    public ResponseEntity<ApiResponse<UserResponse>> update(
        @PathVariable Long id,
        @RequestBody RegisterRequest request) {

        UserResponse user = userService.update(id, request);

        return ResponseEntity.ok(ApiMapper.success("sukses melakukan perubahan data", user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Hapus user berdasarkan ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        userService.delete(id);

        return ResponseEntity.ok(ApiMapper.success("sukses menghapus data"));
    }
}
