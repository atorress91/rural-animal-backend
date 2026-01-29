package com.project.demo.logic.entity.veterinaryAppointment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Clase DTO que representa una cita veterinaria.
 * Almacena información sobre la cita, incluyendo la identificación, nombre del veterinario,
 * apellidos, correo electrónico, especialidad y estado de la cita, así como la fecha y hora de inicio y fin.
 */
@Getter
@Setter
public class VeterinaryAppointmentDto {
    private Long id;
    private String veterinaryName;
    private String firstSurname;
    private String secondSurname;
    private String email;
    private String speciality;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String fullName;
}
