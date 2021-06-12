package com.example.repository;

import com.example.domain.JobLogLevel;
import com.example.entity.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JobLogRepository extends JpaRepository<JobLog, Integer> {

    @Query("SELECT jobLog FROM JobLog AS jobLog WHERE " +
            "(:id IS NULL OR jobLog.id = :id) AND " +
            "(:startDate IS NULL OR jobLog.date >= :startDate) AND " +
            "(:endDate IS NULL OR jobLog.date <= :endDate) AND " +
            "(:logLevel IS NULL OR jobLog.logLevel = :logLevel) AND " +
            "(:jobKey IS NULL OR jobLog.jobKey LIKE %:jobKey%) AND " +
            "(:triggerKey IS NULL OR jobLog.triggerKey LIKE %:triggerKey%) AND " +
            "(:errorMessage IS NULL OR jobLog.errorMessage LIKE %:errorMessage%)")
    List<JobLog> findAllByParameters(@Param("id") Integer id,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("logLevel") JobLogLevel logLevel,
                                     @Param("jobKey") String jobKey,
                                     @Param("triggerKey") String triggerKey,
                                     @Param("errorMessage") String errorMessage);
}
