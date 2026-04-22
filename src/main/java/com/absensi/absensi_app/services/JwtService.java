package com.absensi.absensi_app.services;

import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String extractId(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateToken(Map<String, Object> extractClaims, User userDetails);
    String generateToken(User userDetails);
    boolean isTokenValid(String token, UserResponse userDetails);
}
