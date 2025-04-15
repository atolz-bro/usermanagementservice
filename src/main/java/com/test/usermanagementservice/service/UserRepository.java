package com.test.usermanagementservice.service;

import com.test.usermanagementservice.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByUsername(String username);
}
