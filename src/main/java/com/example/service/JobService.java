package com.example.service;

import com.example.domain.JobExecutionDetails;
import com.example.domain.JobLogLevel;
import com.example.domain.TriggerDto;
import com.example.entity.JobLog;
import com.example.exception.JobDetailNotFoundException;
import com.example.exception.TriggerNotFoundException;
import com.example.exception.ValidationException;
import com.example.util.TriggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Log4j2
@Service
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler;
    private final JobLogService jobLogService;

    public boolean scheduleJob(TriggerDto triggerDto) {
        try {
            verifyJobDetailExists(triggerDto.getJobId(), triggerDto.getJobGroupName());
            TriggerUtils.validateTriggerDto(triggerDto);
            Trigger trigger = TriggerUtils.convertDtoToTrigger(triggerDto);
            scheduler.scheduleJob(trigger);
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.INFO)
                    .jobKey(trigger.getKey().toString())
                    .triggerKey(trigger.getKey().toString())
                    .errorMessage("Job successfully scheduled")
                    .build();
            jobLogService.log(jobLog);
            return true;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(triggerDto.getJobId(), triggerDto.getJobGroupName()).toString())
                    .triggerKey(new TriggerKey(triggerDto.getTriggerId(), triggerDto.getTriggerGroupName()).toString())
                    .errorMessage("Error while scheduling job. Message: " + e.getMessage())
                    .build();
            log.error("Error while scheduling job. Message: {}", e.getMessage());
            jobLogService.log(jobLog);
            return false;
        }
    }

    //todo: think about refactoring, because this method looks ugly :(
    public boolean stopScheduledJob(String triggerId, String triggerGroupName) {
        JobKey jobKey;
        try {
            jobKey = getJobKeyForTriggerKey(new TriggerKey(triggerId, triggerGroupName));
        } catch (SchedulerException e) {
            log.warn("Error while getting details for scheduled jobs. Message: {}", e.getMessage());
            jobLogService.log(JobLogLevel.WARN, "Error while getting details for scheduled jobs. Message: " + e.getMessage());
            return false;
        }
        try {
            checkTriggerExists(triggerId, triggerGroupName);
            boolean isJobStopped = scheduler.unscheduleJob(new TriggerKey(triggerId, triggerGroupName));
            JobLog jobLog = JobLog.builder()
                    .logLevel(isJobStopped ? JobLogLevel.INFO : JobLogLevel.WARN)
                    .triggerKey(new TriggerKey(triggerId, triggerGroupName).toString())
                    .jobKey(jobKey.toString())
                    .errorMessage(isJobStopped ? "Job successfully stopped" : "Job stopping failed")
                    .build();
            jobLogService.log(jobLog);
            return isJobStopped;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(jobKey.toString())
                    .triggerKey(new TriggerKey(triggerId, triggerGroupName).toString())
                    .errorMessage("Error while stopping job. Message: " + e.getMessage())
                    .build();
            log.error("Error while stopping job. Message: {}", e.getMessage());
            jobLogService.log(jobLog);
            return false;
        }
    }

    public boolean updateScheduledJob(String triggerId, String triggerGroupName, TriggerDto triggerDto) {
        try {
            checkTriggerExists(triggerId, triggerGroupName);
            checkNewTriggerJobKey(triggerId, triggerGroupName, triggerDto);
            TriggerUtils.validateTriggerDto(triggerDto);
            Trigger newTrigger = TriggerUtils.convertDtoToTrigger(triggerDto);
            scheduler.rescheduleJob(new TriggerKey(triggerId, triggerGroupName), newTrigger);
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.INFO)
                    .jobKey(newTrigger.getJobKey().toString())
                    .triggerKey(newTrigger.getKey().toString())
                    .errorMessage("Job successfully rescheduled")
                    .build();
            jobLogService.log(jobLog);
            return true;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(triggerDto.getJobId(), triggerDto.getJobGroupName()).toString())
                    .triggerKey(new TriggerKey(triggerId, triggerGroupName).toString())
                    .errorMessage("Error while rescheduling job. Message: " + e.getMessage())
                    .build();
            log.error("Error while rescheduling job. Message: {}", e.getMessage());
            jobLogService.log(jobLog);
            return false;
        }
    }

    public List<JobExecutionDetails> getScheduledJobs() {
        Set<Trigger> triggers = null;
        try {
            triggers = getAllTriggers();
        } catch (SchedulerException e) {
            log.warn("Error while getting info about scheduled jobs. Message: {}", e.getMessage());
            jobLogService.log(JobLogLevel.WARN, "Error while getting info about scheduled jobs. Message: " + e.getMessage());
        }
        return CollectionUtils.isEmpty(triggers)
                ? emptyList()
                : getJobExecutionDetailsMap(triggers);
    }

    private List<JobExecutionDetails> getJobExecutionDetailsMap(Set<Trigger> triggers) {
        return triggers.stream()
                .map(trigger -> {
                    JobDetail jobDetail;
                    try {
                        jobDetail = scheduler.getJobDetail(trigger.getJobKey());
                    } catch (SchedulerException e) {
                        log.warn("Error while getting info about scheduled jobs. Message: {}", e.getMessage());
                        jobLogService.log(JobLogLevel.WARN, "Error while getting info about scheduled jobs. Message: " + e.getMessage());
                        return null;
                    }
                    return new AbstractMap.SimpleEntry<>(trigger, jobDetail);
                })
                .filter(Objects::nonNull)
                .map(entry -> buildJobExecutionDetails(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private JobExecutionDetails buildJobExecutionDetails(Trigger trigger, JobDetail jobDetail) {
        return JobExecutionDetails.builder()
                .triggerClassName(trigger.getClass().getName())
                .triggerKey(trigger.getKey())
                .triggerDescription(trigger.getDescription())
                .misfireInstruction(trigger.getMisfireInstruction())
                .nextFireTime(trigger.getNextFireTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .jobClassName(jobDetail.getJobClass().getName())
                .jobKey(jobDetail.getKey())
                .jobDetailDescription(jobDetail.getDescription())
                .concurrentExecutionDisallowed(jobDetail.isConcurrentExectionDisallowed())
                .persistJobDataAfterExecution(jobDetail.isPersistJobDataAfterExecution())
                .jobSelfRecovered(jobDetail.requestsRecovery())
                .jobDetailsRemainStoredAfterExecution(jobDetail.isDurable())
                .build();
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

    private Set<Trigger> getAllTriggers() throws SchedulerException {
        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyGroup());
        return triggerKeys.stream()
                .map(triggerKey -> {
                    try {
                        return scheduler.getTrigger(triggerKey);
                    } catch (SchedulerException e) {
                        log.warn("Error while getting info about scheduled jobs. Message: {}", e.getMessage());
                        jobLogService.log(JobLogLevel.WARN, "Error while getting info about scheduled jobs. Message: " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void checkTriggerExists(String triggerId, String triggerGroupName) throws SchedulerException {
        boolean triggerExists = scheduler.checkExists(new TriggerKey(triggerId, triggerGroupName));
        if (!triggerExists) {
            throw new TriggerNotFoundException(
                    "Trigger with such triggerId: " + triggerId + " and triggerGroupName: " + triggerGroupName + " is not found");
        }
    }

    private void checkNewTriggerJobKey(String oldTriggerId, String oldTriggerGroupName, TriggerDto newTriggerDto) throws SchedulerException {
        Trigger oldTrigger = scheduler.getTrigger(new TriggerKey(oldTriggerId, oldTriggerGroupName));
        JobKey jobKey = oldTrigger.getJobKey();
        boolean isJobKeyEquivalent =
                jobKey.equals(new JobKey(newTriggerDto.getJobId(), newTriggerDto.getTriggerGroupName()));
        if (!isJobKeyEquivalent) {
            throw new ValidationException("Specified jobId: " + newTriggerDto.getJobId() + " and jobGroupName: " +
                    newTriggerDto.getTriggerGroupName() + " for new trigger are not correct." +
                    "Specified jobId and jobGroupName must be equivalent for new trigger and for old trigger");
        }
    }

    private JobKey getJobKeyForTriggerKey(TriggerKey triggerKey) throws SchedulerException {
        return scheduler.getTrigger(triggerKey).getJobKey();
    }
}
