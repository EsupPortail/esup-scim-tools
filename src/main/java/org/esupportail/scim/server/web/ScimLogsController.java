package org.esupportail.scim.server.web;

import jakarta.annotation.Resource;
import org.esupportail.scim.server.logging.ScimRequestResponseLogger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
public class ScimLogsController {

    @Resource
    ScimRequestResponseLogger scimRequestResponseLogger;

    @GetMapping("/stream-scim-logs")
    public SseEmitter streamSseMvc() {
        return scimRequestResponseLogger.getEmitter();
    }

}
