package com.example.controller;

import com.example.domain.JobDetailDto;
import com.example.service.JobDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("jobDetail")
@RequiredArgsConstructor
public class JobDetailController {

    private final JobDetailService jobDetailService;

    @GetMapping("{jobId}/{jobGroupName}")
    public ResponseEntity<List<JobDetailDto>> getJobDetailList(@PathVariable(required = false) String jobId,
                                                               @PathVariable(required = false) String jobGroupName) {
        List<JobDetailDto> jobDetailList = jobDetailService.getJobDetailList(jobId, jobGroupName);
        return new ResponseEntity<>(jobDetailList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Boolean> saveJobDetail(@RequestBody JobDetailDto jobDetailDto) {
        boolean isJobDetailSaved = jobDetailService.saveJobDetail(jobDetailDto);
        return new ResponseEntity<>(isJobDetailSaved, HttpStatus.OK);
    }

    @DeleteMapping("{jobId}/{jobGroupName}")
    public ResponseEntity<Boolean> deleteJobDetail(@PathVariable String jobId, @PathVariable String jobGroupName) {
        boolean isJobDetailDeleted = jobDetailService.deleteJobDetail(jobId, jobGroupName);
        return new ResponseEntity<>(isJobDetailDeleted, HttpStatus.OK);
    }
}
