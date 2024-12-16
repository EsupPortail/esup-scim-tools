package org.esupportail.scim.server.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.esupportail.scim.server.web.ScimRequestResponseLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class ScimRequestResponseLogger extends OncePerRequestFilter {

    Logger log = LoggerFactory.getLogger(ScimRequestResponseLogger.class);

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    JsonPrettyPrinter jsonPrettyPrinter = new JsonPrettyPrinter();

    Set<SseEmitter> emitters = new HashSet<>();
    
    private final int maxPayloadLength = 1000000;

    public SseEmitter getEmitter() {
        SseEmitter emitter = new SseEmitter(-1L);
        emitters.add(emitter);
        return emitter;
    }

    private String getContentAsString(byte[] buf, int maxLength, String charsetName) {
        if (buf == null || buf.length == 0) return "";
        int length = Math.min(buf.length, this.maxPayloadLength);
        try {
            return new String(buf, 0, length, charsetName);
        } catch (UnsupportedEncodingException ex) {
            return "Unsupported Encoding";
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(!request.getRequestURI().startsWith("/scim2")) {
            filterChain.doFilter(request, response);
        } else {
            long startTime = System.currentTimeMillis();
            StringBuffer reqInfo = new StringBuffer()
                    .append(request.getMethod())
                    .append(" ")
                    .append(request.getRequestURL());

            String queryString = request.getQueryString();
            if (queryString != null) {
                reqInfo.append("?").append(queryString);
            }
            
            log.info("=> " + reqInfo);
            
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

            filterChain.doFilter(wrappedRequest, wrappedResponse);// ======== This performs the actual request!
            long duration = System.currentTimeMillis() - startTime;



            String requestBody = new String(wrappedRequest.getContentAsByteArray());
            if (requestBody.length() > 0) {
                log.info("   Request body:\n" + requestBody);
            }

            int responseStatus = response.getStatus();
            log.info("<= " + reqInfo + ": returned status=" + responseStatus + " in " + duration + "ms");
            byte[] buf = wrappedResponse.getContentAsByteArray();
            String responseBody = new String(buf);
            log.info("   Response body:\n" + responseBody);

            // IMPORTANT: copy content of response back into original response
            wrappedResponse.copyBodyToResponse();

            ScimRequestResponseLog requestResponse = new ScimRequestResponseLog(new Date(),
                    duration,
                    reqInfo.toString(),
                    jsonPrettyPrinter.prettyPrint(requestBody),
                    responseStatus,
                    jsonPrettyPrinter.prettyPrint(responseBody));
            sendSseEvent(requestResponse);

        }
    }

    private void sendSseEvent(ScimRequestResponseLog requestResponse) {
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .data(requestResponse)
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name("scimlogs");
        Set<SseEmitter> deadEmitters = new HashSet<>();
        for(SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (Throwable t) {
                log.debug("Error sending event to emitter {}", emitter, t);
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

}