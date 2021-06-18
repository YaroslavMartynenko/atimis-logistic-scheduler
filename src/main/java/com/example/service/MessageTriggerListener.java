package com.example.service;

import lombok.RequiredArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

@RequiredArgsConstructor
public class MessageTriggerListener implements TriggerListener {

    private final JobService jobService;

    @Override
    public String getName() {
        return MessageTriggerListener.class.getSimpleName();
    }

    //this method updates job each time when trigger is fired
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        String jobId = trigger.getKey().getName();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        //todo here can be added logic for logging job execution
//        if (!timerInfo.isRunForever()) {
//            int remainingFireCount = timerInfo.getRemainingFireCount();
//            if (remainingFireCount == 0) {
//                return;
//            }
//            timerInfo.setRemainingFireCount(remainingFireCount - 1);
//        }
//        schedulerService.updateTimer(timerId, timerInfo);
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {

    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {

    }
}
