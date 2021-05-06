package com.example.service;

import com.example.domain.JobDetailDto;
import com.example.util.JobDetailUtils;
import lombok.RequiredArgsConstructor;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
public class JobDetailService {

    private final Scheduler scheduler;

    public List<JobDetailDto> getJobDetailList(String jobId, String jobGroupName) {
        Set<JobKey> jobKeys = null;
        try {
            //todo: replace on apache common lang
            jobKeys = !StringUtils.isEmpty(jobId)
                    ? Collections.singleton(new JobKey(jobId, jobGroupName))
                    : StringUtils.isEmpty(jobGroupName)
                        ? scheduler.getJobKeys(GroupMatcher.anyGroup())
                        : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName));
        } catch (SchedulerException e) {
            //todo: add logging
        }

        return CollectionUtils.isEmpty(jobKeys)
                ? emptyList()
                : jobKeys.stream()
                .map(jobKey -> {
                    try {
                        return scheduler.getJobDetail(jobKey);
                    } catch (SchedulerException e) {
                        //todo: add logging
                        return null;
                    }
                })
                .map(JobDetailUtils::convertJobDetailToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean saveJobDetail(JobDetailDto jobDetailDto) {
        try {
            JobDetailUtils.validateJobDetailDto(jobDetailDto);
            JobDetail jobDetail = JobDetailUtils.convertDtoToJobDetail(jobDetailDto);
            scheduler.addJob(jobDetail, false, true);
            return true;
        } catch (SchedulerException | ClassNotFoundException e) {
            //todo: add logging
            return false;
        }
    }

    public boolean deleteJobDetail(String jobId, String jobGroupName) {
        try {
            return scheduler.deleteJob(new JobKey(jobId, jobGroupName));
        } catch (SchedulerException e) {
            e.printStackTrace();
            //todo: add logging
            return false;
        }
    }
}
