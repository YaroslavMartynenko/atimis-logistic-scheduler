package com.example.util;

import com.example.domain.JobDetailDto;
import com.example.exception.ValidationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.quartz.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobDetailUtils {

    public static JobDetailDto convertJobDetailToDto(JobDetail jobDetail) {
        if (Objects.isNull(jobDetail)) {
            return null;
        }

        return JobDetailDto.builder()
                .jobClassName(jobDetail.getJobClass().getSimpleName())
                .jobId(jobDetail.getKey().getName())
                .jobGroupName(jobDetail.getKey().getGroup())
                .jobData(jobDetail.getJobDataMap().getWrappedMap())
                .isJobSelfRecovered(jobDetail.requestsRecovery())
                .isJobDurable(jobDetail.isDurable())
                .description(jobDetail.getDescription())
                .build();
    }

    @SuppressWarnings("unchecked")
    public static JobDetail convertDtoToJobDetail(JobDetailDto jobDetailDto) throws ClassNotFoundException {
        if (Objects.isNull(jobDetailDto)) {
            return null;
        }

        JobKey jobKey = StringUtils.isEmpty(jobDetailDto.getJobGroupName())
                ? new JobKey(jobDetailDto.getJobId()) : new JobKey(jobDetailDto.getJobId(), jobDetailDto.getJobGroupName());

        JobDataMap jobDataMap = CollectionUtils.isEmpty(jobDetailDto.getJobData())
                ? new JobDataMap() : new JobDataMap(jobDetailDto.getJobData());

        boolean requestRecovery = !Objects.isNull(jobDetailDto.getIsJobSelfRecovered()) && jobDetailDto.getIsJobSelfRecovered();
        boolean storeDurably = !Objects.isNull(jobDetailDto.getIsJobDurable()) && jobDetailDto.getIsJobDurable();
        String description = StringUtils.isEmpty(jobDetailDto.getDescription()) ? null : jobDetailDto.getDescription();

        return JobBuilder
                //todo: "com.example.job." - it is hotfix. Needs find out some better solution
                // As possible solution: add to JobDetailDto field String classLocation
                .newJob((Class<? extends Job>) Class.forName("com.example.job." + jobDetailDto.getJobClassName()))
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .requestRecovery(requestRecovery)
                .storeDurably(storeDurably)
                .withDescription(description)
                .build();
    }

    public static void validateJobDetailDto(JobDetailDto jobDetailDto) {
        validateJobClassName(jobDetailDto.getJobClassName());
        validateJobId(jobDetailDto.getJobId());
    }

    private static void validateJobClassName(String jobClassName) {
        if (StringUtils.isEmpty(jobClassName)) {
            throw new ValidationException("Required value \"jobClassName\" is not specified or empty");
        }

        Class<?> jobClass;
        try {
            //todo: "com.example.job." - it is hotfix. Needs find out some better solution
            jobClass = Class.forName("com.example.job." + jobClassName);
        } catch (ClassNotFoundException e) {
            throw new ValidationException(
                    "Specified value \"jobClassName\" is incorrect. Job class with such name does not exist");
        }

        if (!Job.class.isAssignableFrom(jobClass)) {
            throw new ValidationException(
                    "Specified value \"jobClassName\" is incorrect. Class with such name does not implement \"Job\" interface");
        }
    }

    private static void validateJobId(String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            throw new ValidationException("Required value \"jobId\" is not specified or empty");
        }
    }
}
