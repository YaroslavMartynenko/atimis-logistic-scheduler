package com.example.controller;

import com.example.domain.JobExecutionDetails;
import com.example.domain.TriggerDto;
import com.example.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("job")
public class JobController {

    private final JobService jobService;

    @GetMapping("info")
    public ResponseEntity<List<JobExecutionDetails>> getScheduledJobs() {
        List<JobExecutionDetails> scheduledJobs = jobService.getScheduledJobs();
        return new ResponseEntity<>(scheduledJobs, HttpStatus.OK);
    }

    @PostMapping("run")
    public ResponseEntity<Boolean> scheduleJob(@RequestBody TriggerDto triggerDto) {
        boolean isJobScheduled = jobService.scheduleJob(triggerDto);
        return new ResponseEntity<>(isJobScheduled, HttpStatus.OK);
    }

    @PostMapping("stop/{triggerId}/{triggerGroupName}")
    public ResponseEntity<Boolean> stopScheduledJob(@PathVariable String triggerId,
                                                    @PathVariable String triggerGroupName) {
        boolean isJobStopped = jobService.stopScheduledJob(triggerId, triggerGroupName);
        return new ResponseEntity<>(isJobStopped, HttpStatus.OK);
    }

    @PostMapping("pause/{jobId}/{jobGroupName}")
    public ResponseEntity<Boolean> pauseJob(@PathVariable String jobId, @PathVariable String jobGroupName) {
        boolean isJobPaused = jobService.pauseJob(jobId, jobGroupName);
        return new ResponseEntity<>(isJobPaused, HttpStatus.OK);
    }

    @PostMapping("resume/{jobId}/{jobGroupName}")
    public ResponseEntity<Boolean> resumeJob(@PathVariable String jobId, @PathVariable String jobGroupName) {
        boolean isJobResumed = jobService.resumeJob(jobId, jobGroupName);
        return new ResponseEntity<>(isJobResumed, HttpStatus.OK);
    }

    @PostMapping("pauseTrigger/{triggerId}/{triggerGroupName}")
    public ResponseEntity<Boolean> pauseTrigger(@PathVariable String triggerId, @PathVariable String triggerGroupName) {
        boolean isTriggerPaused = jobService.pauseTrigger(triggerId, triggerGroupName);
        return new ResponseEntity<>(isTriggerPaused, HttpStatus.OK);
    }

    @PostMapping("resumeTrigger/{triggerId}/{triggerGroupName}")
    public ResponseEntity<Boolean> resumeTrigger(@PathVariable String triggerId, @PathVariable String triggerGroupName) {
        boolean isTriggerResumed = jobService.resumeTrigger(triggerId, triggerGroupName);
        return new ResponseEntity<>(isTriggerResumed, HttpStatus.OK);
    }

    @PutMapping("update/{triggerId}/{triggerGroupName}")
    public ResponseEntity<Boolean> updateJob(@PathVariable String triggerId,
                                             @PathVariable String triggerGroupName,
                                             @RequestBody TriggerDto triggerDto) {
        boolean isJobUpdated = jobService.updateScheduledJob(triggerId, triggerGroupName, triggerDto);
        return new ResponseEntity<>(isJobUpdated, HttpStatus.OK);
    }
}
