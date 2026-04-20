package com.absensi.absensi_app.repository;

import com.absensi.absensi_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.parser.Entity;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail (String email);
    User findByEmail (String email);
}
