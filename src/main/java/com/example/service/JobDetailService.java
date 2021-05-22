package com.example.service;

import com.example.domain.JobDetailDto;
import com.example.domain.JobLogLevel;
import com.example.entity.JobLog;
import com.example.util.JobDetailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Log4j2
@Service
@RequiredArgsConstructor
public class JobDetailService {

    private final Scheduler scheduler;
    private final JobLogger jobLogger;

    public List<JobDetailDto> getJobDetailList(String jobId, String jobGroupName) {
        Set<JobKey> jobKeys = null;
        try {
            jobKeys = StringUtils.isNotEmpty(jobId)
                    ? Collections.singleton(new JobKey(jobId, jobGroupName))
                    : StringUtils.isEmpty(jobGroupName)
                        ? scheduler.getJobKeys(GroupMatcher.anyGroup())
                        : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName));
        } catch (SchedulerException e) {
            log.warn("Error while getting jobDetail list. Message: {}", e.getMessage());
            jobLogger.log(JobLogLevel.WARN, "Error while getting jobDetail list. Message: " + e.getMessage());
        }

        return CollectionUtils.isEmpty(jobKeys)
                ? emptyList()
                : jobKeys.stream()
                .map(jobKey -> {
                    try {
                        return scheduler.getJobDetail(jobKey);
                    } catch (SchedulerException e) {
                        log.warn("Error while getting jobDetail list. Message: {}", e.getMessage());
                        jobLogger.log(JobLogLevel.WARN, "Error while getting jobDetail list. Message: " + e.getMessage());
                        return null;
                    }
                })
                .map(JobDetailUtils::convertJobDetailToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean saveJobDetail(JobDetailDto jobDetailDto) {
        try {
            JobDetailUtils.validateJobDetailDto(jobDetailDto);
            JobDetail jobDetail = JobDetailUtils.convertDtoToJobDetail(jobDetailDto);
            scheduler.addJob(jobDetail, false, true);
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.INFO)
                    .jobKey(new JobKey(jobDetailDto.getJobId(), jobDetailDto.getJobGroupName()).toString())
                    .errorMessage("Job detail successfully saved")
                    .build();
            jobLogger.log(jobLog);
            return true;
        } catch (SchedulerException | ClassNotFoundException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(jobDetailDto.getJobId(), jobDetailDto.getJobGroupName()).toString())
                    .errorMessage("Error while saving jobDetail. Message: " + e.getMessage())
                    .build();
            log.error("Error while saving jobDetail. Message: {}", e.getMessage());
            jobLogger.log(jobLog);
            return false;
        }
    }

    public boolean deleteJobDetail(String jobId, String jobGroupName) {
        try {
            boolean isJobDeleted = scheduler.deleteJob(new JobKey(jobId, jobGroupName));
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.INFO)
                    .jobKey(new JobKey(jobId, jobGroupName).toString())
                    .errorMessage(isJobDeleted ? "Job detail successfully deleted" : "Job detail deleting failed")
                    .build();
            jobLogger.log(jobLog);
            return isJobDeleted;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(jobId, jobGroupName).toString())
                    .errorMessage("Error while deleting jobDetail. Message: " + e.getMessage())
                    .build();
            log.error("Error while deleting jobDetail. Message: {}", e.getMessage());
            jobLogger.log(jobLog);
            return false;
        }
    }
}
