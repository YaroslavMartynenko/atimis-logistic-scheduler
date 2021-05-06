package com.example.service;

import com.example.domain.TimerInfo;
import com.example.domain.TriggerDto;
import com.example.exception.JobDetailNotFoundException;
import com.example.exception.TriggerNotFoundException;
import com.example.job.MessageJob;
import com.example.util.TimerUtils;
import com.example.util.TriggerUtils;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler;

    public boolean scheduleJob(String jobId, String jobGroupName, TriggerDto triggerDto) {
        try {
            TriggerUtils.validateTriggerDto(triggerDto);
            Trigger trigger = TriggerUtils.convertDtoToTrigger(triggerDto);
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobId, jobGroupName));
            if (isNull(jobDetail)) {
                throw new JobDetailNotFoundException(
                        "JobDetail with such jobId: " + jobId + " and jobGroupName: " + jobGroupName + " is not found");
            }
            scheduler.scheduleJob(jobDetail, Collections.singleton(trigger), true);
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

    //todo: check
    public List<JobExecutionContext> getScheduledJobs() {
        try {
//          return context.getScheduler().getCurrentlyExecutingJobs();
            return scheduler.getCurrentlyExecutingJobs();
        } catch (SchedulerException e) {
            e.printStackTrace();
            //todo: add logging
            return null;
        }
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
