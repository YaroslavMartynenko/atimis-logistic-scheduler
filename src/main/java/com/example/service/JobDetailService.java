package com.example.service;

import com.example.domain.JobDetailDto;
import com.example.domain.JobLogLevel;
import com.example.entity.JobLog;
import com.example.exception.JobDetailNotFoundException;
import com.example.exception.ValidationException;
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
import static java.util.Objects.nonNull;

@Log4j2
@Service
@RequiredArgsConstructor
public class JobDetailService {

    private final Scheduler scheduler;
    private final JobLogService jobLogService;

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
            jobLogService.log(JobLogLevel.WARN, "Error while getting jobDetail list. Message: " + e.getMessage());
        }

        return CollectionUtils.isEmpty(jobKeys)
                ? emptyList()
                : jobKeys.stream()
                .map(jobKey -> {
                    try {
                        return scheduler.getJobDetail(jobKey);
                    } catch (SchedulerException e) {
                        log.warn("Error while getting jobDetail list. Message: {}", e.getMessage());
                        jobLogService.log(JobLogLevel.WARN, "Error while getting jobDetail list. Message: " + e.getMessage());
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
            jobLogService.log(jobLog);
            return true;
        } catch (SchedulerException | ClassNotFoundException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(jobDetailDto.getJobId(), jobDetailDto.getJobGroupName()).toString())
                    .errorMessage("Error while saving jobDetail. Message: " + e.getMessage())
                    .build();
            log.error("Error while saving jobDetail. Message: {}", e.getMessage());
            jobLogService.log(jobLog);
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
            jobLogService.log(jobLog);
            return isJobDeleted;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(jobId, jobGroupName).toString())
                    .errorMessage("Error while deleting jobDetail. Message: " + e.getMessage())
                    .build();
            log.error("Error while deleting jobDetail. Message: {}", e.getMessage());
            jobLogService.log(jobLog);
            return false;
        }
    }

    public boolean updateJobDetail(JobDetailDto jobDetailDto) {
        try {
            String jobId = jobDetailDto.getJobId();
            String jobGroupName = jobDetailDto.getJobGroupName();
            verifyJobDetailExists(jobId, jobGroupName);
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobId, jobGroupName));
            JobDetail updatedJobDetail = JobBuilder
                    .newJob(jobDetail.getJobClass())
                    .withIdentity(jobDetail.getKey())
                    .usingJobData(nonNull(jobDetailDto.getJobData())
                            ? new JobDataMap(jobDetailDto.getJobData()) : jobDetail.getJobDataMap())
                    .requestRecovery(nonNull(jobDetailDto.getIsJobSelfRecovered())
                            ? jobDetailDto.getIsJobSelfRecovered() : jobDetail.requestsRecovery())
                    .storeDurably(nonNull(jobDetailDto.getIsJobDurable())
                            ? jobDetailDto.getIsJobDurable() : jobDetail.isDurable())
                    .withDescription(StringUtils.isNotEmpty(jobDetailDto.getDescription())
                            ? jobDetailDto.getDescription() : jobDetail.getDescription())
                    .build();

            scheduler.addJob(updatedJobDetail, true, true);
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.INFO)
                    .jobKey(new JobKey(jobId, jobGroupName).toString())
                    .errorMessage("Job detail successfully updated")
                    .build();
            jobLogService.log(jobLog);
            return true;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(jobDetailDto.getJobId(), jobDetailDto.getJobGroupName()).toString())
                    .errorMessage("Error while updating jobDetail. Message: " + e.getMessage())
                    .build();
            log.error("Error while updating jobDetail. Message: {}", e.getMessage());
            jobLogService.log(jobLog);
            return false;
        }

    }

    private void verifyJobDetailExists(String jobId, String jobGroupName) throws SchedulerException {
        if (StringUtils.isEmpty(jobId) || StringUtils.isEmpty(jobGroupName)) {
            throw new ValidationException("Required value \"jobId\" or \"jobGroupName\" is not specified or empty");
        }
        boolean jobDetailExists = scheduler.checkExists(new JobKey(jobId, jobGroupName));
        if (!jobDetailExists) {
            throw new JobDetailNotFoundException(
                    "JobDetail with such jobId: " + jobId + " and jobGroupName: " + jobGroupName + " is not found");
        }
    }
}
