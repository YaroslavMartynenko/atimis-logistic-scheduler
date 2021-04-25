package com.example.controller;

import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("trigger")
@RequiredArgsConstructor
public class TriggerController {

    private final Scheduler scheduler;
}
