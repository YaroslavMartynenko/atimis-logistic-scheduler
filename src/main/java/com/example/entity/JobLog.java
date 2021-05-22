package com.example.entity;

import com.example.domain.JobLogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "atimis_logistic", name = "job_logs")
public class JobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "date")
    @CreationTimestamp
    private LocalDateTime date;
    @Column(name = "log_level")
    @Enumerated(EnumType.STRING)
    private JobLogLevel logLevel;
    @Column(name = "job_key")
    private String jobKey;
    @Column(name = "trigger_key")
    private String triggerKey;
    @Column(name = "error_message")
    private String errorMessage;
}
