package com.example.controller;

import com.example.domain.TriggerDto;
import com.example.service.JobService;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("job")
public class JobController {

    private final JobService jobService;

    // This endpoint does not work correctly
    @GetMapping("info")
    public ResponseEntity<List<JobExecutionContext>> getScheduledJobs(JobExecutionContext context) {
        List<JobExecutionContext> scheduledJobs = jobService.getScheduledJobs();
        return new ResponseEntity<>(scheduledJobs, HttpStatus.OK);
    }

    @PostMapping("run/{jobId}/{jobGroupName}")
    public ResponseEntity<Boolean> scheduleJob(@PathVariable String jobId,
                                               @PathVariable String jobGroupName,
                                               @RequestBody TriggerDto triggerDto) {
        boolean isJobScheduled = jobService.scheduleJob(jobId, jobGroupName, triggerDto);
        return new ResponseEntity<>(isJobScheduled, HttpStatus.OK);
    }

    @PostMapping("stop/{triggerId}/{triggerGroupName}")
    public ResponseEntity<Boolean> stopScheduledJob(@PathVariable String triggerId,
                                                    @PathVariable String triggerGroupName) {
        boolean isJobStopped = jobService.stopScheduledJob(triggerId, triggerGroupName);
        return new ResponseEntity<>(isJobStopped, HttpStatus.OK);
    }

    @PostMapping("runHardcodedJob")
    public ResponseEntity<Boolean> scheduleJob() {
        boolean isJobScheduled = jobService.scheduleHardcodedJob();
        return new ResponseEntity<>(isJobScheduled, HttpStatus.OK);
    }

    //todo: check if possible reschedule job and implement this endpoint
//    @PutMapping("{jobId}")//todo: change endpoint path and update service logic
//    public ResponseEntity<Boolean> updateJob(@PathVariable String jobId, @RequestBody TimerInfo timerInfo) {
//        boolean isJobUpdated = jobService.updateScheduledJob(jobId, timerInfo);
//        return new ResponseEntity<>(isJobUpdated, HttpStatus.OK);
//    }
}
