package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionDetails {

    private String jobClassName;
    private JobKey jobKey;
    private String triggerClassName;
    private TriggerKey triggerKey;
    //other fields
    //todo: override and implement pretty toString method
}
