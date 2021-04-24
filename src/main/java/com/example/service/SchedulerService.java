package com.example.service;

import com.example.domain.JobDetailDto;
import com.example.domain.TimerInfo;
import com.example.job.MessageJob;
import com.example.util.JobDetailUtils;
import com.example.util.TimerUtils;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final Scheduler scheduler;

    @PostConstruct
    public void startScheduler() {
        try {
            scheduler.start();
//            scheduler.getListenerManager().addTriggerListener(new MessageTriggerListener(this));
        } catch (SchedulerException e) {
            System.out.println(e.getMessage());
        }
    }

    @PreDestroy
    public void stopScheduler() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            System.out.println(e.getMessage());
        }
    }




    public boolean scheduleJob() {
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

    public boolean stopScheduledJob(String triggerId) {
        try {
            return scheduler.checkExists(new TriggerKey(triggerId)) && scheduler.unscheduleJob(new TriggerKey(triggerId));
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateScheduledJob(String jobId, TimerInfo info) {
        try {
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobId));
            if (Objects.isNull(jobDetail)) {
                return false;
            }
            jobDetail.getJobDataMap().put(jobId, info);
            scheduler.addJob(jobDetail, true, true);
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteJob(String jobId) {
        try {
            return scheduler.deleteJob(new JobKey(jobId));
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TimerInfo> getAllScheduledJobsDetail() {
        try {
            return scheduler.getJobKeys(GroupMatcher.anyGroup())
                    .stream()
                    .map(jobKey -> {
                        try {
                            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                            return (TimerInfo) jobDetail.getJobDataMap().get(jobKey.getName());
                        } catch (SchedulerException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public TimerInfo getScheduledJobDetail(String jobId) {
        try {
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobId));
            return Objects.nonNull(jobDetail)
                    ? (TimerInfo) jobDetail.getJobDataMap().get(jobId)
                    : null;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return null;
        }
    }
}
