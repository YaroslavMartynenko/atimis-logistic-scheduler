package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionDetails {

    private String triggerClassName;
    private TriggerKey triggerKey;
    private String triggerDescription;
    private Integer misfireInstruction;
    private LocalDateTime nextFireTime;
    private String jobClassName;
    private JobKey jobKey;
    private String jobDetailDescription;
    private Boolean concurrentExecutionDisallowed;
    private Boolean persistJobDataAfterExecution;
    private Boolean jobSelfRecovered;
    private Boolean jobDetailsRemainStoredAfterExecution;
}
