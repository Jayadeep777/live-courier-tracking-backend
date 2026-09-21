package com.logistics.auth.repository;

import com.logistics.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<User> findByRole(com.logistics.auth.model.Role role);
    java.util.List<User> findByRoleAndStatus(com.logistics.auth.model.Role role, String status);
}
