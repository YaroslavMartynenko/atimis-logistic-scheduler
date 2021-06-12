package com.example.service;

import com.example.domain.JobLogDto;
import com.example.domain.JobLogLevel;
import com.example.domain.JobLogParameters;
import com.example.entity.JobLog;
import com.example.repository.JobLogRepository;
import com.example.util.JobLogUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class JobLogService {

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

    public List<JobLogDto> getLogsByParameters(JobLogParameters parameters) {
        JobLogUtils.validateJobLogParameters(parameters);
        List<JobLog> jobLogs = jobLogRepository.findAllByParameters(
                parameters.getId(),
                nonNull(parameters.getStartDate()) ? LocalDateTime.parse(parameters.getStartDate()) : null,
                nonNull(parameters.getEndDate()) ? LocalDateTime.parse(parameters.getEndDate()) : null,
                nonNull(parameters.getJobLogLevel()) ? JobLogLevel.valueOf(parameters.getJobLogLevel()) : null,
                parameters.getJobKey(),
                parameters.getTriggerKey(),
                parameters.getErrorMessage());

        return CollectionUtils.isEmpty(jobLogs)
                ? emptyList()
                : jobLogs.stream().map(JobLogUtils::convertJobLogToDto).collect(Collectors.toList());
    }
}
