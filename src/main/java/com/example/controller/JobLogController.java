package com.example.controller;

import com.example.domain.JobLogDto;
import com.example.domain.JobLogParameters;
import com.example.service.JobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("log")
public class JobLogController {

    private final JobLogService jobLogService;

    @PostMapping("find")
    public ResponseEntity<List<JobLogDto>> getLogsByParameters( @RequestBody JobLogParameters parameters) {
        List<JobLogDto> jobLogDtoList = jobLogService.getLogsByParameters(parameters);
        return new ResponseEntity<>(jobLogDtoList, HttpStatus.OK);
    }
}
