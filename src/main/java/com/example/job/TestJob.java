package com.example.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
//todo remove after testing
public class TestJob implements Job {
    @Override
    public void execute(JobExecutionContext context)  {
        System.out.println("It works? current time: " + LocalDateTime.now().toString());
    }
}
