package org.esupportail.scim.server.web;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public record ScimRequestResponseLog(
    @JsonFormat(pattern="yyyy-MM-dd / HH:mm:ss") Date date,
    long duration,
    String requestUrl,
    String requestBody,
    int responseStatus,
    String responseBody
) {
}

