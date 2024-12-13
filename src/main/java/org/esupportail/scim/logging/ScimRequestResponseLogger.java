package org.esupportail.scim.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
public class ScimRequestResponseLogger extends OncePerRequestFilter {

    Logger log = LoggerFactory.getLogger(ScimRequestResponseLogger.class);

    SseEmitter emitter = new SseEmitter(-1L);
    
    private final int maxPayloadLength = 1000;

    public SseEmitter getEmitter() {
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

            filterChain.doFilter(wrappedRequest, wrappedResponse);     // ======== This performs the actual request!
            long duration = System.currentTimeMillis() - startTime;
            
            String requestBody = this.getContentAsString(wrappedRequest.getContentAsByteArray(), this.maxPayloadLength, request.getCharacterEncoding());
            if (requestBody.length() > 0) {
                log.info("   Request body:\n" + requestBody);
            }

            log.info("<= " + reqInfo + ": returned status=" + response.getStatus() + " in " + duration + "ms");
            byte[] buf = wrappedResponse.getContentAsByteArray();
            String responseBody = this.getContentAsString(buf, this.maxPayloadLength, response.getCharacterEncoding());
            log.info("   Response body:\n" + responseBody);

            sendSseEvent(reqInfo.toString(), requestBody, response.getStatus(), responseBody, duration);

            wrappedResponse.copyBodyToResponse();  // IMPORTANT: copy content of response back into original response
        }
    }

    private void sendSseEvent(String requestPath, String requestBody, int responseStatus, String responseBody, long duration) {
        try {
            String requestWellFormed = String.format("Request: %s", requestPath);
            if(requestBody.length() > 0) {
                requestWellFormed += String.format("\n%s", requestBody);
            }
            String responseWellFormed = String.format("Response: %d\n%s", responseStatus, responseBody);
            String requestResponse = String.format("%s ms\n%s\n%s\n\n", duration, requestWellFormed, responseWellFormed);
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .data(requestResponse)
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name("scimlogs");
            emitter.send(event);
        } catch (IOException e) {
            log.error("Error sending SSE event", e);
        }
    }

}