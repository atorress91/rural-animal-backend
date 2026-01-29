package com.project.demo.logic.entity.transaction;

import com.project.demo.logic.entity.publication.TblPublication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TblTransactionRepository extends JpaRepository<TblTransaction, Long> {

    // Obtener las transacciones aprobadas de un usuario
    @Query("SELECT t FROM TblTransaction t WHERE t.status != 'PENDING' AND t.user.id = :id")
    Page<TblTransaction> findTblTransactionsByUserId(@Param("id") Long id, Pageable pageable);

    // Obtener todas las transacciones aprobadas
    @Query("SELECT t FROM TblTransaction t WHERE t.status != 'PENDING'")
    Page<TblTransaction> findAll(Pageable pageable);
}
