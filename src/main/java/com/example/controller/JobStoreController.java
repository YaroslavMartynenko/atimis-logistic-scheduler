package com.example.controller;

import com.example.domain.JobDetailDto;
import com.example.service.JobStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("job/store")
@RequiredArgsConstructor
public class JobStoreController {

    private final JobStoreService jobStoreService;

    @GetMapping
    public ResponseEntity<List<JobDetailDto>> getJobDetailList(@RequestParam(required = false) String jobGroupName) {
        List<JobDetailDto> jobDetailList = jobStoreService.getJobDetailList(jobGroupName);
        return new ResponseEntity<>(jobDetailList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Boolean> saveJobDetail(@RequestBody JobDetailDto jobDetailDto) {
        boolean isJobDetailSaved = jobStoreService.saveJobDetails(jobDetailDto);
        return new ResponseEntity<>(isJobDetailSaved, HttpStatus.OK);
    }
}
