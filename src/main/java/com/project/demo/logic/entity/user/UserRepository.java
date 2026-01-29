package com.project.demo.logic.entity.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<TblUser, Long>  {
    // Obtener usuario por email
    Optional<TblUser> findByEmail(String email);

    // Obtener todos los usuarios activos
    @Query("SELECT u FROM TblUser u")
    Page<TblUser> findAll(Pageable pageable);

    @Query("SELECT u FROM TblUser u " +
            "LEFT JOIN u.role r " +
            "LEFT JOIN u.direction d " +
            "WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName1) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName2) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.identification) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.province) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.canton) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.district) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.otherDetails) LIKE LOWER(CONCAT('%', :keyword, '%'))")

    Page<TblUser> findUsersByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
