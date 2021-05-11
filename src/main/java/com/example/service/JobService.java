package com.example.service;

import com.example.domain.TimerInfo;
import com.example.domain.TriggerDto;
import com.example.exception.JobDetailNotFoundException;
import com.example.exception.TriggerNotFoundException;
import com.example.exception.ValidationException;
import com.example.job.MessageJob;
import com.example.util.TimerUtils;
import com.example.util.TriggerUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Service
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler;

    public boolean scheduleJob(TriggerDto triggerDto) {
        try {
            String jobId = triggerDto.getJobId();
            String jobGroupName = triggerDto.getJobGroupName();
            if (StringUtils.isEmpty(jobId) || StringUtils.isEmpty(jobGroupName)) {
                throw new ValidationException("Required value \"jobId\" or \"jobGroupName\" is not specified or empty");
            }
            boolean jobDetailExists = scheduler.checkExists(new JobKey(jobId, jobGroupName));
            if (!jobDetailExists) {
                throw new JobDetailNotFoundException(
                        "JobDetail with such jobId: " + jobId + " and jobGroupName: " + jobGroupName + " is not found");
            }
            TriggerUtils.validateTriggerDto(triggerDto);
            Trigger trigger = TriggerUtils.convertDtoToTrigger(triggerDto);
            scheduler.scheduleJob(trigger);
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            //todo: add logging
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
            return scheduler.unscheduleJob(new TriggerKey(triggerId, triggerGroupName));
        } catch (SchedulerException e) {
            e.printStackTrace();
            //todo: add logging
            return false;
        }
    }

    //todo: think about simplifying method. Create some pretty dto object instead of map - JobExecutionDetails
    public Map<String, String> getScheduledJobs() {
        Set<TriggerKey> triggerKeys = null;
        try {
            triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyGroup());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        if (CollectionUtils.isEmpty(triggerKeys)) {
            return emptyMap();
        }

        List<Trigger> triggers = triggerKeys.stream()
                .map(triggerKey -> {
                    try {
                        return scheduler.getTrigger(triggerKey);
                    } catch (SchedulerException e) {
                        //todo: add logging
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return triggers.stream()
                .map(trigger -> {
                    JobDetail jobDetail;
                    try {
                        jobDetail = scheduler.getJobDetail(trigger.getJobKey());
                    } catch (SchedulerException e) {
                        //todo: add logging
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
