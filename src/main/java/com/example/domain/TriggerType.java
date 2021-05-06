package com.example.domain;

import com.example.configuration.TriggerTypeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

//@JsonDeserialize(using = TriggerTypeDeserializer.class)
public enum TriggerType {
    SIMPLE, CRON
}
