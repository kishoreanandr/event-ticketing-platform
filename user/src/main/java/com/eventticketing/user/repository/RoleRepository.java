package com.eventticketing.user.repository;

import com.eventticketing.user.entity.Role;
import com.eventticketing.user.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByRoleName(RoleName roleName);
}
