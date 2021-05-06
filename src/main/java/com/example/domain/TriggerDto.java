package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDto {

    private String triggerId;
    private String triggerGroupName;
    private TriggerType triggerType;
    private Boolean repeatForever;
    private Integer repeatCount;
    private Long repeatIntervalMs;
    //todo: change on String
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;
    private String cronExpression;
    // other fields for creating trigger
}
