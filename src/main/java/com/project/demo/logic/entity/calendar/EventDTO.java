package com.project.demo.logic.entity.calendar;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String summary;
    private String description;
}