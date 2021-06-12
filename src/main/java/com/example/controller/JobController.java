package com.example.controller;

import com.example.domain.TriggerDto;
import com.example.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("job")
public class JobController {

    private final JobService jobService;

    @GetMapping("info")
    public ResponseEntity<Map<String, String>> getScheduledJobs() {
        Map<String, String> scheduledJobs = jobService.getScheduledJobs();
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

    @PutMapping("update/{triggerId}/{triggerGroupName}")
    public ResponseEntity<Boolean> updateJob(@PathVariable String triggerId,
                                             @PathVariable String triggerGroupName,
                                             @RequestBody TriggerDto triggerDto) {
        boolean isJobUpdated = jobService.updateScheduledJob(triggerId, triggerGroupName, triggerDto);
        return new ResponseEntity<>(isJobUpdated, HttpStatus.OK);
    }
}
