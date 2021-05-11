package com.example.job;

import com.example.entity.Message;
import com.example.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class MessageJob implements Job {

    private final MessageService messageService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        System.out.println("Thread name: "+ Thread.currentThread().getName());
        Message message = Message.builder().message("This message was created on: " + LocalDateTime.now()).build();
        messageService.saveMessage(message);
    }
}
