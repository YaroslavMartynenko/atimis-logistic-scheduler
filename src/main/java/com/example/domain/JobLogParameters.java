package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobLogParameters {

    private Integer id;
    private String startDate;
    private String endDate;
    private String jobLogLevel;
    private String jobKey;
    private String triggerKey;
    private String errorMessage;
}
