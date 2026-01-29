package com.project.demo.logic.entity.notification;

import com.project.demo.logic.entity.publication.TblPublication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<TblNotification, Long> {
    Page<TblNotification> findByUserId(Long userId, Pageable pageable);

    // Obtener todas las notificaciones activas
    @Query("SELECT p FROM TblNotification p WHERE p.state = 'Active' AND p.user.id = :userId")
    List<TblNotification> findActiveByUserId(@Param("userId") Long userId);
}
