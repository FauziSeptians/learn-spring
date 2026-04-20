package com.absensi.absensi_app.repository;

import com.absensi.absensi_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Boolean existsByEmail(String email);

  User findByEmail(String email);
}
