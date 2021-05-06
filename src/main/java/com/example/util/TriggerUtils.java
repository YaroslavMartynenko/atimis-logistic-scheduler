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

    public static void validateTriggerDto(TriggerDto triggerDto) {
        if (isNull(triggerDto)) {
            throw new ValidationException("Required trigger dto object is null");
        }
        validateTriggerId(triggerDto.getTriggerId());
        validateTriggerType(triggerDto.getTriggerType());
        validateDtoValuesByTriggerType(triggerDto);
    }

    private static void validateTriggerId(String triggerId) {
        if (StringUtils.isEmpty(triggerId)) {
            throw new ValidationException("Required value \"triggerId\" is not specified or empty");
        }
    }

    //todo: trigger type validation does not work correctly due to problem with TriggerType deserializer
    private static void validateTriggerType(TriggerType triggerType) {
        if (isNull(triggerType)) {
            throw new ValidationException("Required value \"triggerType\" is specified incorrectly or empty");
        }
    }

    private static void validateDtoValuesByTriggerType(TriggerDto triggerDto) {
        Consumer<TriggerDto> triggerValidator = TRIGGER_VALIDATORS.get(triggerDto.getTriggerType());
        if (isNull(triggerValidator)) {
            throw new IllegalArgumentException("Can not validate trigger dto: " + triggerDto.toString());
        }
        triggerValidator.accept(triggerDto);
    }

    private static void validateDtoValuesForSimpleTrigger(TriggerDto triggerDto) {
        validateRepeatForeverValue(triggerDto.getRepeatForever());
    }

    private static void validateDtoValuesForCronTrigger(TriggerDto triggerDto) {
        try {
            CronExpression.validateExpression(triggerDto.getCronExpression());
        } catch (ParseException e) {
            throw new ValidationException("Required value \"cronExpression\" is specified incorrectly or empty");
        }
        //todo: validate start and end date, dates should be in future, end date should be after start date
        //todo: move validation for each value to separate method
    }

    private static void validateRepeatForeverValue(Boolean repeatForever) {
        if (isNull(repeatForever)) {
            throw new ValidationException("Required value \"repeatForever\" is not specified or empty");
        }
        //todo:
        //if repeatForever=true then validate repeatInterval
        //if repeatForever=true then validate start and end date
        //if repeatForever=false then validate repeatCount
        //todo: move validation for each value to separate method
    }

    public static Trigger convertDtoToTrigger(TriggerDto triggerDto) {
        Function<TriggerDto, Trigger> triggerConvertor = TRIGGER_CONVERTORS.get(triggerDto.getTriggerType());
        if (isNull(triggerConvertor)) {
            throw new IllegalArgumentException("Can not create trigger from trigger dto: " + triggerDto.toString());
        }
        return triggerConvertor.apply(triggerDto);
    }

    private static Trigger createSimpleTrigger(TriggerDto triggerDto) {

        SimpleScheduleBuilder schedulerBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMilliseconds(triggerDto.getRepeatIntervalMs())
                //this instruction force trigger re-execute job now if something went wrong
                .withMisfireHandlingInstructionNowWithRemainingCount();

        schedulerBuilder = triggerDto.getRepeatForever()
                ? schedulerBuilder.repeatForever() : schedulerBuilder.withRepeatCount(triggerDto.getRepeatCount());

        TriggerKey triggerKey = StringUtils.isEmpty(triggerDto.getTriggerGroupName())
                ? new TriggerKey(triggerDto.getTriggerId())
                : new TriggerKey(triggerDto.getTriggerId(), triggerDto.getTriggerGroupName());

        return TriggerBuilder
                .newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(schedulerBuilder)
                .startAt(Timestamp.valueOf(triggerDto.getStartDate()))
                .withDescription(triggerDto.getDescription())
                .build();
    }

    private static Trigger createCronTrigger(TriggerDto triggerDto) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule(triggerDto.getCronExpression())
                //this instruction force trigger re-execute job now if something went wrong
                .withMisfireHandlingInstructionFireAndProceed();

        TriggerKey triggerKey = StringUtils.isEmpty(triggerDto.getTriggerGroupName())
                ? new TriggerKey(triggerDto.getTriggerId())
                : new TriggerKey(triggerDto.getTriggerId(), triggerDto.getTriggerGroupName());

        return TriggerBuilder
                .newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(scheduleBuilder)
                .startAt(Timestamp.valueOf(triggerDto.getStartDate()))
                .endAt(Timestamp.valueOf(triggerDto.getEndDate()))
                .withDescription(triggerDto.getDescription())
                .build();
    }
}
