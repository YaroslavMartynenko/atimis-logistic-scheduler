package com.example.util;

import com.example.domain.JobLogDto;
import com.example.domain.JobLogLevel;
import com.example.domain.JobLogParameters;
import com.example.entity.JobLog;
import com.example.exception.ValidationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobLogUtils {

    public static JobLogDto convertJobLogToDto(JobLog jobLog) {
        return JobLogDto.builder()
                .id(jobLog.getId())
                .date(jobLog.getDate().toString())
                .jobLogLevel(jobLog.getLogLevel().name())
                .jobKey(jobLog.getJobKey())
                .triggerKey(jobLog.getTriggerKey())
                .errorMessage(jobLog.getErrorMessage())
                .build();
    }

    public static void validateJobLogParameters(JobLogParameters parameters) {
        if (isNull(parameters)) {
            throw new ValidationException("Required JobLogParameters object is null");
        }
        String startDate = parameters.getStartDate();
        String endDate = parameters.getEndDate();
        if (nonNull(startDate)) {
            validateStartDate(startDate);
        }
        if (nonNull(endDate)) {
            validateEndDate(endDate);
        }
        if (nonNull(startDate) && nonNull(endDate)) {
            validateStartAndEndDateOrder(startDate, endDate);
        }
        if (nonNull(parameters.getJobLogLevel())) {
            validateJobLogLevel(parameters.getJobLogLevel());
        }
    }

    private static void validateStartDate(String startDateString) {
        try {
            LocalDateTime.parse(startDateString);
        } catch (Exception e) {
            throw new ValidationException("Value \"startDate\" is specified incorrectly or empty");
        }
    }

    private static void validateEndDate(String endDateString) {
        try {
            LocalDateTime.parse(endDateString);
        } catch (Exception e) {
            throw new ValidationException("Value \"endDate\" is specified incorrectly or empty");
        }
    }

    private static void validateStartAndEndDateOrder(String startDateString, String endDateString) {
        LocalDateTime startDate = LocalDateTime.parse(startDateString);
        LocalDateTime endDate = LocalDateTime.parse(endDateString);
        boolean isEndDateAfterStartDate = endDate.isAfter(startDate);
        if (!isEndDateAfterStartDate) {
            throw new ValidationException("Value \"startDate\" and  \"endDate\" are specified incorrectly, " +
                    "\"endDate\" must be after \"startDate\"");
        }
    }

    private static void validateJobLogLevel(String jobLogLevel) {
        try {
            JobLogLevel.valueOf(jobLogLevel);
        } catch (Exception e) {
            throw new ValidationException("Value \"jobLogLevel\" is specified incorrectly or empty");
        }
    }
}
