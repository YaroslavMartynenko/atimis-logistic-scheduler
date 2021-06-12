package com.example.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobLogDto {

    private final Integer id;
    private final String date;
    private final String jobLogLevel;
    private final String jobKey;
    private final String triggerKey;
    private final String errorMessage;
}
