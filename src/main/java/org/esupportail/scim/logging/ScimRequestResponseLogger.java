package org.esupportail.scim.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
public class ScimRequestResponseLogger extends OncePerRequestFilter {

    Logger log = LoggerFactory.getLogger(ScimRequestResponseLogger.class);
    
    private int maxPayloadLength = 1000;

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
                    .append("[")
                    .append(startTime % 10000)  // request ID
                    .append("] ")
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
            log.info("   Response body:\n" + getContentAsString(buf, this.maxPayloadLength, response.getCharacterEncoding()));

            wrappedResponse.copyBodyToResponse();  // IMPORTANT: copy content of response back into original response
        }
    }

}