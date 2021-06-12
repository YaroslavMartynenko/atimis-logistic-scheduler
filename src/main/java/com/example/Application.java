package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);
//        JobLogger bean = run.getBean(JobLogger.class);
//        JobLog jobLog = JobLog.builder()
//                .logLevel(JobLogLevel.INFO)
//                .jobKey(new JobKey("jobId", "jobGroupName").toString())
//                .triggerKey(new TriggerKey("triggerId", "triggerGroupName").toString())
//                .errorMessage("Job successfully scheduled")
//                .build();
//
//        bean.log(jobLog);

//        JobLogger bean = run.getBean(JobLogger.class);
//        List<JobLog> allLogs = bean.findLogsByParameters();
//        allLogs.forEach(l -> System.out.println(l));
    }
}
