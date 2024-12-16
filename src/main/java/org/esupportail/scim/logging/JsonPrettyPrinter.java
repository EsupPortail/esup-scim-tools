package org.esupportail.scim.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

public class JsonPrettyPrinter {

    Logger log = org.slf4j.LoggerFactory.getLogger(JsonPrettyPrinter.class);

    ObjectMapper objectMapper = new ObjectMapper();

    public String prettyPrint(String uglyJsonString) {
        if(uglyJsonString==null || uglyJsonString.isBlank()) {
            return "";
        }
        try {
            Object jsonObject = objectMapper.readValue(uglyJsonString, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Throwable e) {
            log.warn("Failed to pretty print JSON : {}", uglyJsonString, e);
            return uglyJsonString;
        }
    }
}
