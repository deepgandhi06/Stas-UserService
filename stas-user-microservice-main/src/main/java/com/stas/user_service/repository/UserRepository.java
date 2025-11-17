package com.stas.user_service.repository;

import com.stas.user_service.entity.User;
import com.stas.user_service.enums.RoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // non-paged list
    List<User> findByRole(RoleEnum role);

    // paged variant (use Spring's Page and Pageable)
    Page<User> findByRole(RoleEnum role, Pageable pageable);

    // fast count
    long countByRole(RoleEnum role);
}
