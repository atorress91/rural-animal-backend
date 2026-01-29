package com.project.demo.logic.entity.veterinaryAppointment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa un DTO que contiene información sobre la disponibilidad en una fecha específica
 * y los intervalos de tiempo disponibles para esa fecha.
 */
@Getter
@Setter
public class AvailabilityDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    private List<TimeSlotDto> availableSlots;
}