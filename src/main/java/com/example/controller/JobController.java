package com.example.controller;

import com.example.domain.TimerInfo;
import com.example.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("job")
public class JobController {

    private final SchedulerService schedulerService;

    @PostMapping("run")
    public ResponseEntity<Boolean> scheduleJob() {
        boolean isJobScheduled = schedulerService.scheduleJob();
        return new ResponseEntity<>(isJobScheduled, HttpStatus.OK);
    }

    @PostMapping("stop/{triggerId}")
    public ResponseEntity<Boolean> stopScheduledJob(@PathVariable String triggerId) {
        boolean isJobStopped = schedulerService.stopScheduledJob(triggerId);
        return new ResponseEntity<>(isJobStopped, HttpStatus.OK);
    }

    @DeleteMapping("{jobId}")
    public ResponseEntity<Boolean> deleteJob(@PathVariable String jobId) {
        boolean isJobDeleted = schedulerService.deleteJob(jobId);
        return new ResponseEntity<>(isJobDeleted, HttpStatus.OK);
    }

    @PutMapping("{jobId}")//todo: change endpoint path and update service logic, names
    public ResponseEntity<Boolean> updateJob(@PathVariable String jobId, @RequestBody TimerInfo timerInfo) {
        boolean isJobUpdated = schedulerService.updateScheduledJob(jobId, timerInfo);
        return new ResponseEntity<>(isJobUpdated, HttpStatus.OK);
    }

    @GetMapping("jobDetail1")//todo: change endpoint path and update service logic, names
    public ResponseEntity<List<TimerInfo>> getAllScheduledJobsDetail() {
        List<TimerInfo> jobsDetails = schedulerService.getAllScheduledJobsDetail();
        return new ResponseEntity<>(jobsDetails, HttpStatus.OK);
    }

    @GetMapping("jobDetail1/{jobId}")//todo: change endpoint path and update service logic, names
    public ResponseEntity<TimerInfo> getScheduledJobDetail(@PathVariable String jobId) {
        TimerInfo jobDetail = schedulerService.getScheduledJobDetail(jobId);
        return new ResponseEntity<>(jobDetail, HttpStatus.OK);
    }


}
