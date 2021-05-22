package com.example.service;

import com.example.domain.JobLogLevel;
import com.example.entity.JobLog;
import com.example.repository.JobLogRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobLogger {

    private final JobLogRepository jobLogRepository;

    public void log(@NonNull JobExecutionContext context, @NonNull JobLogLevel logLevel, @NonNull String message) {
        JobLog jobLog = JobLog.builder()
                .logLevel(logLevel)
                .jobKey(context.getJobDetail().getKey().toString())
                .triggerKey(context.getTrigger().getKey().toString())
                .errorMessage(message)
                .build();
        jobLogRepository.save(jobLog);
    }

    public void log(@NonNull JobLog jobLog) {
        jobLogRepository.save(jobLog);
    }

    public void log(@NonNull JobLogLevel logLevel, @NonNull String message) {
        JobLog jobLog = JobLog.builder()
                .logLevel(logLevel)
                .errorMessage(message)
                .build();
        jobLogRepository.save(jobLog);
    }

    public void log(@NonNull JobLogLevel logLevel, @NonNull String jobKey, @NonNull String triggerKey, @NonNull String message) {
        JobLog jobLog = JobLog.builder()
                .logLevel(logLevel)
                .jobKey(jobKey)
                .triggerKey(triggerKey)
                .errorMessage(message)
                .build();
        jobLogRepository.save(jobLog);
    }
    //todo: add logic for managing logs
}
