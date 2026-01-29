package com.project.demo.logic.entity.veterinaryAppointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TblVeterinaryAppointmentRepository extends JpaRepository<TblVeterinaryAppointment, Long> {

    /**
     * Método para encontrar citas veterinarias asociadas a un usuario específico.
     *
     * @param userId   El id del usuario cuyas citas veterinarias se quieren encontrar.
     * @param pageable Objeto que contiene información de paginación.
     * @return Una página de citas veterinarias que pertenecen al usuario dado.
     */
    @Query("SELECT va FROM TblVeterinaryAppointment va WHERE va.user.id = :userId")
    Page<TblVeterinaryAppointment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Encuentra citas veterinarias que se superponen con el rango de fechas proporcionado para un
     * veterinario específico.
     *
     * @param veterinaryId El identificador único del veterinario para el cual se busca superposición de citas.
     * @param startDate    La fecha y hora de inicio del rango para verificar superposición.
     * @param endDate      La fecha y hora de finalización del rango para verificar superposición.
     * @return Una lista de objetos TblVeterinaryAppointment que representan las citas que se superponen
     * con el rango de fechas especificado. Si no se encuentran citas que se superpongan, la
     * lista estará vacía.
     */
    @Query("SELECT va FROM TblVeterinaryAppointment va " +
            "WHERE va.veterinary.id = :veterinaryId " +
            "AND va.startDate < :endDate " +
            "AND va.endDate > :startDate")
    List<TblVeterinaryAppointment> findOverlappingAppointments(
            @Param("veterinaryId") Long veterinaryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Busca citas veterinarias que ocurren dentro de un rango de fechas especificado.
     *
     * @param startDate La fecha y hora de inicio del rango de búsqueda.
     * @param endDate   La fecha y hora de fin del rango de búsqueda.
     * @return Una lista de objetos TblVeterinaryAppointment que están programados para ocurrir
     * entre las fechas de inicio y fin proporcionadas.
     */
    @Query("SELECT va FROM TblVeterinaryAppointment va WHERE va.startDate < :endDate AND va.endDate > :startDate")
    List<TblVeterinaryAppointment> findAppointmentsInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
