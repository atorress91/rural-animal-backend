package com.project.demo.logic.entity.role;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TblRoleRepository extends CrudRepository<TblRole, Integer> {
    Optional<TblRole> findByTitle(RoleEnum title);
}
