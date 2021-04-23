package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);
//        MessageServiceImpl messageService = run.getBean(MessageServiceImpl.class);
//        Message message = Message.builder().message("This message was created on: "+ LocalDateTime.now()).build();
//        messageService.saveMessage(message);
    }
}
