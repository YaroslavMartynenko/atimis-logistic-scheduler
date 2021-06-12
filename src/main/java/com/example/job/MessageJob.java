package com.example.job;

import com.example.domain.JobLogLevel;
import com.example.entity.Message;
import com.example.service.JobLogService;
import com.example.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Log4j2
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class MessageJob implements Job {

    private final MessageService messageService;
    private final JobLogService jobLogService;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            Message message = Message.builder().message("This message was created on: " + LocalDateTime.now()).build();
            messageService.saveMessage(message);
            jobLogService.log(context, JobLogLevel.INFO, "MessageJob successfully executed");
        } catch (Exception e) {
            log.error("Error while executing MessageJob. JobExecutionContext: {} Message: {}", context, e.getMessage());
            jobLogService.log(context, JobLogLevel.ERROR, "Error while executing MessageJob. Message: " + e.getMessage());
        }
    }
}
