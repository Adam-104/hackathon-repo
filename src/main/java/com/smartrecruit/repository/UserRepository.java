package com.smartrecruit.repository;

import com.smartrecruit.model.User;
import com.smartrecruit.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);

    long countByEnabledFalse();

    List<User> findAllByOrderByCreatedAtDesc();
}
