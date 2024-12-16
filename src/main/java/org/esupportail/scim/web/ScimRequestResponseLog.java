package org.esupportail.scim.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public record ScimRequestResponseLog(
    @JsonFormat(pattern="yyyy-MM-dd") Date date,
    long duration,
    String requestUrl,
    String requestBody,
    int responseStatus,
    String responseBody
) {
}

