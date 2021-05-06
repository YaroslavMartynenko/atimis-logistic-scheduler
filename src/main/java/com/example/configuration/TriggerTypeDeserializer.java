package com.example.configuration;

import com.example.domain.TriggerType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.stream.Stream;

public class TriggerTypeDeserializer extends StdDeserializer<TriggerType> {

    public TriggerTypeDeserializer() {
       this(null);
    }

    public TriggerTypeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TriggerType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        String triggerTypeName = jsonNode.get("triggerType").asText();
        return Stream.of(TriggerType.values())
                .filter(triggerType -> triggerType.name().equals(triggerTypeName))
                .findFirst()
                .orElse(null);
    }
}
