package com.marcoscode.elearning.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
     boolean existsByEmail(String adminEmail);

    Optional<User> findByEmail(String email);

}
