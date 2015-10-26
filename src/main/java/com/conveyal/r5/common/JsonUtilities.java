package com.conveyal.r5.common;

import com.conveyal.r5.model.json_serialization.BitSetSerializer;
import com.conveyal.r5.model.json_serialization.JodaLocalDateSerializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by matthewc on 10/23/15.
 */
public class JsonUtilities {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.registerModule(JodaLocalDateSerializer.makeModule());
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore JSON fields that don't match target type
    }
}