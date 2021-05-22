package com.example.service;

import com.example.domain.JobLogLevel;
import com.example.domain.TimerInfo;
import com.example.domain.TriggerDto;
import com.example.entity.JobLog;
import com.example.exception.JobDetailNotFoundException;
import com.example.exception.TriggerNotFoundException;
import com.example.exception.ValidationException;
import com.example.job.MessageJob;
import com.example.util.TimerUtils;
import com.example.util.TriggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler;
    private final JobLogger jobLogger;

    public boolean scheduleJob(TriggerDto triggerDto) {
        try {
            verifyJobDetailExists(triggerDto.getJobId(), triggerDto.getJobGroupName());
            TriggerUtils.validateTriggerDto(triggerDto);
            Trigger trigger = TriggerUtils.convertDtoToTrigger(triggerDto);
            scheduler.scheduleJob(trigger);
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.INFO)
                    .jobKey(new JobKey(triggerDto.getJobId(), triggerDto.getJobGroupName()).toString())
                    .triggerKey(new TriggerKey(triggerDto.getTriggerId(), triggerDto.getTriggerGroupName()).toString())
                    .errorMessage("Job successfully scheduled")
                    .build();
            jobLogger.log(jobLog);
            return true;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .jobKey(new JobKey(triggerDto.getJobId(), triggerDto.getJobGroupName()).toString())
                    .triggerKey(new TriggerKey(triggerDto.getTriggerId(), triggerDto.getTriggerGroupName()).toString())
                    .errorMessage("Error while scheduling job. Message: " + e.getMessage())
                    .build();
            log.error("Error while scheduling job. Message: {}", e.getMessage());
            jobLogger.log(jobLog);
            return false;
        }
    }

    public boolean stopScheduledJob(String triggerId, String triggerGroupName) {
        try {
            boolean triggerExists = scheduler.checkExists(new TriggerKey(triggerId, triggerGroupName));
            if (!triggerExists) {
                throw new TriggerNotFoundException(
                        "Trigger with such triggerId: " + triggerId + " and triggerGroupName: " + triggerGroupName + " is not found");
            }
            boolean isJobStopped = scheduler.unscheduleJob(new TriggerKey(triggerId, triggerGroupName));
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.INFO)
                    .triggerKey(new TriggerKey(triggerId, triggerGroupName).toString())
                    //todo: think about adding jobKey
                    .errorMessage(isJobStopped ? "Job successfully stopped" : "Job stopping failed")
                    .build();
            jobLogger.log(jobLog);
            return isJobStopped;
        } catch (SchedulerException e) {
            JobLog jobLog = JobLog.builder()
                    .logLevel(JobLogLevel.ERROR)
                    .triggerKey(new TriggerKey(triggerId, triggerGroupName).toString())
                    .errorMessage("Error while stopping job. Message: " + e.getMessage())
                    .build();
            log.error("Error while stopping job. Message: {}", e.getMessage());
            jobLogger.log(jobLog);
            return false;
        }
    }

    //todo: Create some pretty dto object instead of map - JobExecutionDetails
    public Map<String, String> getScheduledJobs() {
        Set<Trigger> triggers = null;
        try {
            triggers = getAllTriggers();
        } catch (SchedulerException e) {
            log.warn("Error while getting info about scheduled jobs. Message: {}", e.getMessage());
            jobLogger.log(JobLogLevel.WARN, "Error while getting info about scheduled jobs. Message: " + e.getMessage());
        }
        return CollectionUtils.isEmpty(triggers)
                ? emptyMap()
                : triggers.stream()
                    .map(trigger -> {
                        JobDetail jobDetail;
                        try {
                            jobDetail = scheduler.getJobDetail(trigger.getJobKey());
                        } catch (SchedulerException e) {
                            log.warn("Error while getting info about scheduled jobs. Message: {}", e.getMessage());
                            jobLogger.log(JobLogLevel.WARN, "Error while getting info about scheduled jobs. Message: " + e.getMessage());
                            return null;
                        }
                        return new AbstractMap.SimpleEntry<>(trigger.toString(), jobDetail.toString());
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean scheduleHardcodedJob() {
        TimerInfo timerInfo = new TimerInfo();
        timerInfo.setTotalFireCount(15);
        timerInfo.setRemainingFireCount(timerInfo.getTotalFireCount());
        timerInfo.setRunForever(false);
        timerInfo.setRepeatIntervalMs(10000);
        timerInfo.setInitialOffsetMs(3000);
        timerInfo.setCallbackData("Some callback data");

        JobDetail jobDetail = TimerUtils.buildJobDetail(MessageJob.class, timerInfo);
        Trigger trigger = TimerUtils.buildTrigger(MessageJob.class, timerInfo);

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
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

    private Set<Trigger> getAllTriggers() throws SchedulerException {
        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyGroup());
        return triggerKeys.stream()
                .map(triggerKey -> {
                    try {
                        return scheduler.getTrigger(triggerKey);
                    } catch (SchedulerException e) {
                        log.warn("Error while getting info about scheduled jobs. Message: {}", e.getMessage());
                        jobLogger.log(JobLogLevel.WARN, "Error while getting info about scheduled jobs. Message: " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    //todo: check if possible reschedule job and implement this method
//    public boolean updateScheduledJob(String jobId, TimerInfo info) {
//        try {
//            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobId));
//            if (isNull(jobDetail)) {
//                return false;
//            }
//            jobDetail.getJobDataMap().put(jobId, info);
//            scheduler.addJob(jobDetail, true, true);
//            return true;
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
