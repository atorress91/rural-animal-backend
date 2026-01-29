package com.project.demo.logic.entity.veterinaryAppointment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.project.demo.logic.entity.veterinary.VeterinaryAvailabilityDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase DTO que representa un intervalo de tiempo disponible para citas.
 * Incluye la hora de inicio y la hora de fin del intervalo,
 * así como una lista de veterinarios disponibles durante ese tiempo.
 */
@Getter
@Setter
public class TimeSlotDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    private List<VeterinaryAvailabilityDto> availableVeterinarians;
}
