package com.project.demo.logic.entity.veterinaryAppointment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa un DTO para la creación de una cita veterinaria.
 * Contiene la información necesaria para agendar una nueva cita.
 */
@Getter
@Setter
public class CreateAppointmentDto {
    private Long veterinaryId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
