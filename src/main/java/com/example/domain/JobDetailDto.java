package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailDto {
    private String jobClassName;
    private String jobId;
    private String jobGroupName;
    private Map<String, Object> jobData;
    private Boolean isJobSelfRecovered;
    private Boolean isJobDurable;
    private String description;
}
