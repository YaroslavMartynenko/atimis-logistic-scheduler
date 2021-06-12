package com.example.util;

import com.example.domain.TriggerDto;
import com.example.domain.TriggerType;
import com.example.exception.ValidationException;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.quartz.*;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TriggerUtils {

    private static final Map<TriggerType, Function<TriggerDto, Trigger>> TRIGGER_CONVERTORS =
            ImmutableMap.<TriggerType, Function<TriggerDto, Trigger>>builder()
                    .put(TriggerType.SIMPLE, TriggerUtils::createSimpleTrigger)
                    .put(TriggerType.CRON, TriggerUtils::createCronTrigger)
                    .build();

    private static final Map<TriggerType, Consumer<TriggerDto>> TRIGGER_VALIDATORS =
            ImmutableMap.<TriggerType, Consumer<TriggerDto>>builder()
                    .put(TriggerType.SIMPLE, TriggerUtils::validateDtoValuesForSimpleTrigger)
                    .put(TriggerType.CRON, TriggerUtils::validateDtoValuesForCronTrigger)
                    .build();

    public static Trigger convertDtoToTrigger(TriggerDto triggerDto) {
        TriggerType triggerType = TriggerType.valueOf(triggerDto.getTriggerType());
        Function<TriggerDto, Trigger> triggerConvertor = TRIGGER_CONVERTORS.get(triggerType);
        if (isNull(triggerConvertor)) {
            throw new IllegalArgumentException("Can not create trigger from trigger dto: " + triggerDto.toString());
        }
        return triggerConvertor.apply(triggerDto);
    }

    public static void validateTriggerDto(TriggerDto triggerDto) {
        if (isNull(triggerDto)) {
            throw new ValidationException("Required TriggerDto object is null");
        }
        validateTriggerType(triggerDto.getTriggerType());
        validateTriggerId(triggerDto.getTriggerId());
        validateTriggerGroupName(triggerDto.getTriggerGroupName());
        validateDtoValuesByTriggerType(triggerDto);
    }

    private static void validateTriggerId(String triggerId) {
        if (StringUtils.isEmpty(triggerId)) {
            throw new ValidationException("Required value \"triggerId\" is not specified or empty");
        }
    }

    private static void validateTriggerGroupName(String triggerGroupName) {
        if (StringUtils.isEmpty(triggerGroupName)) {
            throw new ValidationException("Required value \"triggerGroupName\" is not specified or empty");
        }
    }

    private static void validateTriggerType(String triggerType) {
        try {
            TriggerType.valueOf(triggerType);
        } catch (Exception e) {
            throw new ValidationException("Required value \"triggerType\" is specified incorrectly or empty");
        }
    }

    private static void validateDtoValuesByTriggerType(TriggerDto triggerDto) {
        TriggerType triggerType = TriggerType.valueOf(triggerDto.getTriggerType());
        Consumer<TriggerDto> triggerValidator = TRIGGER_VALIDATORS.get(triggerType);
        if (isNull(triggerValidator)) {
            throw new IllegalArgumentException("Can not validate trigger dto: " + triggerDto.toString());
        }
        triggerValidator.accept(triggerDto);
    }

    private static void validateDtoValuesForSimpleTrigger(TriggerDto triggerDto) {
        validateRepeatForeverValue(triggerDto.getRepeatForever());
        validateRepeatInterval(triggerDto.getRepeatIntervalMs());
        validateStartDate(triggerDto.getStartDate());
        if (triggerDto.getRepeatForever()) {
            validateEndDate(triggerDto.getEndDate());
            validateStartAndEndDateOrder(triggerDto.getStartDate(), triggerDto.getEndDate());
        } else {
            validateRepeatCount(triggerDto.getRepeatCount());
        }
    }

    private static void validateRepeatCount(Integer repeatCount) {
        if (isNull(repeatCount) || repeatCount == 0) {
            throw new ValidationException("Required value \" repeatCount\" is not specified or zero");
        }
    }

    private static void validateRepeatInterval(Long repeatIntervalMs) {
        if (isNull(repeatIntervalMs) || repeatIntervalMs == 0) {
            throw new ValidationException("Required value \"repeatIntervalMs\" is not specified or zero");
        }
    }

    private static void validateRepeatForeverValue(Boolean repeatForever) {
        if (isNull(repeatForever)) {
            throw new ValidationException("Required value \"repeatForever\" is not specified or empty");
        }
    }

    private static void validateDtoValuesForCronTrigger(TriggerDto triggerDto) {
        validateCronExpression(triggerDto.getCronExpression());
        validateStartDate(triggerDto.getStartDate());
        validateEndDate(triggerDto.getEndDate());
        validateStartAndEndDateOrder(triggerDto.getStartDate(), triggerDto.getEndDate());
    }

    private static void validateCronExpression(String cronExpression) {
        try {
            CronExpression.validateExpression(cronExpression);
        } catch (ParseException e) {
            throw new ValidationException("Required value \"cronExpression\" is specified incorrectly or empty");
        }
    }

    private static void validateStartDate(String startDateString) {
        LocalDateTime startDate;
        try {
            startDate = LocalDateTime.parse(startDateString);
        } catch (Exception e) {
            throw new ValidationException("Required value \"startDate\" is specified incorrectly or empty");
        }
        boolean isFutureDate = startDate.isAfter(LocalDateTime.now());
        if (!isFutureDate) {
            throw new ValidationException("Required value \"startDate\" is specified incorrectly, date must not be past");
        }
    }

    private static void validateEndDate(String endDateString) {
        LocalDateTime endDate;
        try {
            endDate = LocalDateTime.parse(endDateString);
        } catch (Exception e) {
            throw new ValidationException("Required value \"endDate\" is specified incorrectly or empty");
        }
        boolean isFutureDate = endDate.isAfter(LocalDateTime.now());
        if (!isFutureDate) {
            throw new ValidationException("Required value \"endDate\" is specified incorrectly, date must not be past");
        }
    }

    private static void validateStartAndEndDateOrder(String startDateString, String endDateString) {
        LocalDateTime startDate = LocalDateTime.parse(startDateString);
        LocalDateTime endDate = LocalDateTime.parse(endDateString);
        boolean isEndDateAfterStartDate = endDate.isAfter(startDate);
        if (!isEndDateAfterStartDate) {
            throw new ValidationException("Required value \"startDate\" and  \"endDate\" are specified incorrectly, " +
                    "\"endDate\" must be after \"startDate\"");
        }
    }

    private static Trigger createSimpleTrigger(TriggerDto triggerDto) {

        SimpleScheduleBuilder schedulerBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMilliseconds(triggerDto.getRepeatIntervalMs())
                //this instruction force trigger re-execute job now if something went wrong
                .withMisfireHandlingInstructionNowWithRemainingCount();

        schedulerBuilder = triggerDto.getRepeatForever()
                ? schedulerBuilder.repeatForever() : schedulerBuilder.withRepeatCount(triggerDto.getRepeatCount());

        TriggerKey triggerKey = new TriggerKey(triggerDto.getTriggerId(), triggerDto.getTriggerGroupName());

        return triggerDto.getRepeatForever()
                ? TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(schedulerBuilder)
                    .startAt(Timestamp.valueOf(LocalDateTime.parse(triggerDto.getStartDate())))
                    .endAt(Timestamp.valueOf(LocalDateTime.parse(triggerDto.getEndDate())))
                    .withDescription(triggerDto.getDescription())
                    .forJob(triggerDto.getJobId(), triggerDto.getJobGroupName())
                    .build()
                : TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(schedulerBuilder)
                    .startAt(Timestamp.valueOf(LocalDateTime.parse(triggerDto.getStartDate())))
                    .withDescription(triggerDto.getDescription())
                    .forJob(triggerDto.getJobId(), triggerDto.getJobGroupName())
                    .build();
    }

    private static Trigger createCronTrigger(TriggerDto triggerDto) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule(triggerDto.getCronExpression())
                //this instruction force trigger re-execute job now if something went wrong
                .withMisfireHandlingInstructionFireAndProceed();

        TriggerKey triggerKey = new TriggerKey(triggerDto.getTriggerId(), triggerDto.getTriggerGroupName());

        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(scheduleBuilder)
                .startAt(Timestamp.valueOf(triggerDto.getStartDate()))
                .endAt(Timestamp.valueOf(triggerDto.getEndDate()))
                .withDescription(triggerDto.getDescription())
                .forJob(triggerDto.getJobId(), triggerDto.getJobGroupName())
                .build();
    }
}
