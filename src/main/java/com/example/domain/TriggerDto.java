package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String startDate;
    private String endDate;
    private String description;
    private String cronExpression;
    private String jobId;
    private String jobGroupName;
}
